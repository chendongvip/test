package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.dto.User;
import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RabbitController {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldController.class);
    private static final String prefix = "rabbit";

    @Autowired
    //Spring 提供的环境变量 可以读取整个项目的配置文件
    private Environment env;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送简单消息
     */
    @RequestMapping(value = prefix + "/simple/message/send",method = RequestMethod.GET)
    public BaseResponse sendSimpleMessage(@RequestParam String message){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            log.info("待发送的消息：{}",message);
            //设置发送消息的编码格式
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //当未指定任何操作时用于发送操作的默认交换的名称。这里指定交换机的名字
            rabbitTemplate.setExchange(env.getProperty("basic.info.mq.exchange.name"));
            //当未指定任何操作时用于发送操作的默认路由键的值。这里指定路由路由键的值
            rabbitTemplate.setRoutingKey(env.getProperty("basic.info.mq.routing.key.name"));
            //MessageBuilder(消息有效载荷类型).withBody最后的消息正文将是对“body”的直接引用。
            //传输消息格式1
            //Message msg = MessageBuilder.withBody(message.getBytes("utf-8")).build();
            //使用默认路由键向默认交换发送消息。
            //rabbitTemplate.send(msg);
            //传输消息格式2 json的传输格式
            Message meg = MessageBuilder.withBody(objectMapper.writeValueAsBytes(message)).build();
            //将Java对象转换为AMQPMessage并将其发送到具有默认路由键的默认交换
            rabbitTemplate.convertAndSend(meg);
        }catch (Exception e){
            log.info("发送简单消息发生异常：",e.fillInStackTrace());
        }
        return response;
    }

    /**
     * 发送对象消息
     */
    @RequestMapping(value = prefix + "/object/message/send",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse sendObjectMessage(@RequestBody User user){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            log.info("待发送对象消息：{}",user);
            //当未指定任何操作时用于发送操作的默认交换的名称。这里指定交换机的名字
            rabbitTemplate.setExchange(env.getProperty("basic.info.mq.exchange.name"));
            //当未指定任何操作时用于发送操作的默认路由键的值。这里指定路由路由键的值
            rabbitTemplate.setRoutingKey(env.getProperty("basic.info.mq.routing.key.name"));
            //设置发送消息的编码格式
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //设置非持久行消息 防止部到生成环境 版本不一致 即使rabbitmq崩溃了 消息队列还在
            Message msg = MessageBuilder.withBody(objectMapper.writeValueAsBytes(user)).setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(msg);
        }catch (Exception e){
            log.info("发送对象消息 发生异常：",e.fillInStackTrace());
        }
        return response;
    }

    /**
     * 发送多类型字段消息
     */
    @RequestMapping(value = prefix + "/multi/type/message/send",method = RequestMethod.GET)
    public BaseResponse sendMultiTypeMessage(){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            Integer id = 1202;
            String name = "阿修罗2";
            Long longId = 120002L;
            Map<String,Object> dataMap = Maps.newHashMap();
            dataMap.put("id",id);
            dataMap.put("name",name);
            dataMap.put("longId",longId);
            log.info("发送多类型字段消息：{}",dataMap);

            //当未指定任何操作时用于发送操作的默认交换的名称。这里指定交换机的名字
            rabbitTemplate.setExchange(env.getProperty("basic.info.mq.exchange.name"));
            //当未指定任何操作时用于发送操作的默认路由键的值。这里指定路由路由键的值
            rabbitTemplate.setRoutingKey(env.getProperty("basic.info.mq.routing.key.name"));
            //设置发送消息的编码格式
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //设置非持久行消息 防止部到生成环境 版本不一致 即使rabbitmq崩溃了 消息队列还在
            Message msg = MessageBuilder.withBody(objectMapper.writeValueAsBytes(dataMap)).setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(msg);
        }catch (Exception e){
            log.info("发送多类型字段消息 发生异常：",e.fillInStackTrace());
        }
        return response;
    }


}

