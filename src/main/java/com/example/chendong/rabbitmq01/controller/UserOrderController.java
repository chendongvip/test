package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.dto.LogDto;
import com.example.chendong.rabbitmq01.dto.UserOrderDto;
import com.example.chendong.rabbitmq01.entity.UserOrder;
import com.example.chendong.rabbitmq01.mapper.UserOrderMapper;
import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import com.example.chendong.rabbitmq01.service.CommonLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户商城下单
 */
@RestController
public class UserOrderController {

    private static final Logger log = LoggerFactory.getLogger(UserOrderController.class);

    private static final String prefix = "user/order";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Autowired
    private CommonLogService logService;

    @Autowired
    private UserOrderMapper userOrderMapper;

    @RequestMapping(value =  prefix + "/push",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse pushUserOrder(@RequestBody UserOrderDto dto){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            log.info("接受到数据：{}",dto);
            //TODO:用户下单入库
            //设置发送消息的格式
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //指定交换机
            rabbitTemplate.setExchange(env.getProperty("user.order.exchange.name"));
            //指定路由
            rabbitTemplate.setRoutingKey(env.getProperty("user.order.routing.key.name"));
            //创建消息源设置成持久化 发送消息
            Message message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(dto)).setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            //异步发送消息
            rabbitTemplate.convertAndSend(message);


            //TODO:系统级别 -- 日志记录 -- 异步分出去
            //序列化
            /*LogDto logDto = new LogDto("pushUserOrder",objectMapper.writeValueAsString(dto));
            logService.insertLog(logDto);*/
            //设置消息传输格式
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //指定交换机
            rabbitTemplate.setExchange(env.getProperty("log.system.exchange.name"));
            //指定路由
            rabbitTemplate.setRoutingKey(env.getProperty("log.system.routing.key.name"));


            //消息处理
            //消息序列化
            LogDto logDto = new LogDto("pushUserOrder",objectMapper.writeValueAsString(dto));
            rabbitTemplate.convertAndSend(logDto,new MessagePostProcessor(){

                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties properties = message.getMessageProperties();
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setHeader(AbstractJavaTypeMapper.DEFAULT_KEY_CLASSID_FIELD_NAME,LogDto.class);
                    return message;
                }
            });
            //TODO:系统级别 -- 日志记录 -- 异步分出去


            //TODO:还有很多业务逻辑




        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }


    /**
     * 用户商城下单
     */
    @RequestMapping(value = prefix+"/push/dead/queue",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse pushUserOrder2(@RequestBody UserOrderDto dto){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        UserOrder userOrder = new UserOrder();
        try{
            log.info("接收到数据： {}",dto);


            BeanUtils.copyProperties(dto,userOrder);
            userOrder.setStatus(1);
            userOrderMapper.insertSelective(userOrder);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            Integer id=userOrder.getId();

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("user.order.dead.produce.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("user.order.dead.produce.routing.key.name"));

            rabbitTemplate.convertAndSend(id, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties properties=message.getMessageProperties();
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,Integer.class);
                    return message;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }

    /**
     * 用户商城下单-动态TTL设置
     * @param dto
     * @return
     */
    @RequestMapping(value = prefix+"/push/dead/queue/v3",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse pushUserOrderV3(@RequestBody UserOrderDto dto){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        UserOrder userOrder=new UserOrder();
        try {
            BeanUtils.copyProperties(dto,userOrder);
            userOrder.setStatus(1);
            userOrderMapper.insertSelective(userOrder);
            log.info("用户商城下单成功!!");
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Integer id=userOrder.getId();

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("dynamic.dead.produce.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("dynamic.dead.produce.routing.key.name"));

            Long ttl=15000L; //可以用随机数替代

            rabbitTemplate.convertAndSend(id, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties properties=message.getMessageProperties();
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,Integer.class);

                    properties.setExpiration(String.valueOf(ttl));
                    return message;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }
}
