package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeadQueueController {
    private static final Logger log = LoggerFactory.getLogger(MailController.class);

    private static final String prefix = "dead/queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @RequestMapping(value = prefix+"/send",method = RequestMethod.GET)
    public BaseResponse sendMail(){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            //设置消息发送的格式
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            //设置交换机 路由的名称
            rabbitTemplate.setExchange(env.getProperty("simple.produce.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("simple.produce.routing.key.name"));

            String str = "死信队列的消息";

            Message message = MessageBuilder.withBody(str.getBytes("UTF-8")).build();
            rabbitTemplate.convertAndSend(message);

        }catch (Exception e){
            e.printStackTrace();
        }
        log.info("发送消息完毕------");
        return response;
    }
}
