package com.example.chendong.rabbitmq01.rabbitmq;

import com.example.chendong.rabbitmq01.dto.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 消费者类
 */
@Component("simpleListener")
public class SimpleListener implements ChannelAwareMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(SimpleListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();

        try{
            byte[] msg = message.getBody();
            User user = objectMapper.readValue(msg,User.class);
            logger.info("对象信息监听确认机制 监听到消息 ：{}",user);

            channel.basicAck(tag,true);
        }catch (Exception e){
            logger.error("对象信息监听确认机制发生异常：",e.fillInStackTrace());

            channel.basicReject(tag,false);
        }

    }
}
