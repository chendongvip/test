package com.example.chendong.rabbitmq01.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * RabbitMQd的listener
 */
@Component
public class RobbingListener {

    private static final Logger log = LoggerFactory.getLogger(RobbingListener.class);

    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 监听抢单信息
     */
    @RabbitListener(queues = "${product.robbing.mq.queue.name}",containerFactory = "singleListenerContainer")
    public void consumeMessage(@Payload byte[] message){
        try{
            String mobile = new String(message,"UTF-8");
            log.info("监听到抢单手机号：{}",mobile);
        }catch (Exception e){
            log.info("监听抢单信息 发生异常 ： ",e.fillInStackTrace());
        }
    }
}
