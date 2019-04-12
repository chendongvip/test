package com.example.chendong.rabbitmq01.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * 工作消息限流类
 */
@Service
public class CommonMqService {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment environment;

    /**
     * 发送抢单信息到队列
     */
    public void sendRobbingMsg(String mobile){
        try{
            rabbitTemplate.setExchange(environment.getProperty("product.robbing.mq.exchange.name"));
            rabbitTemplate.setRoutingKey(environment.getProperty("product.robbing.mq.routing.key.name"));
            //设置消息持久化
            Message message = MessageBuilder.withBody(mobile.getBytes("UTF-8")).setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                          .build();
            rabbitTemplate.send(message);
        }catch (Exception e){
            log.error("发送抢单信息到队列 发生异常  mobile{} ",mobile);
        }
    }


}
