package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.dto.User;
import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息源 类
 */
@RestController
public class AcknowledgeController {

    private static final Logger log = LoggerFactory.getLogger(AcknowledgeController.class);

    private static final String prefix = "ack";

    //发送消息的组件
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    //读取配置文件中的配置
    @Autowired
    private Environment env;

    @RequestMapping(value = prefix + "/user/info",method = RequestMethod.GET)
    public BaseResponse ackUser(){
        User user=new User(1,"debug","steadyjack");
        try{
            //设置消息传输的格式
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //指定交换机名字
            rabbitTemplate.setExchange(env.getProperty("simple.mq.exchange.name"));
            //指定路由的名字
            rabbitTemplate.setRoutingKey(env.getProperty("simple.mq.routing.key.name"));

            //创建消息源 设置成持久化
            Message message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(user))
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            //发送消息
            rabbitTemplate.convertAndSend(message);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new BaseResponse(StatusCode.Success);
    }
}
