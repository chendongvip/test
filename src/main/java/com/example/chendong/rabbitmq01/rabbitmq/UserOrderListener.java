package com.example.chendong.rabbitmq01.rabbitmq;

import com.example.chendong.rabbitmq01.dto.UserOrderDto;
import com.example.chendong.rabbitmq01.entity.UserOrder;
import com.example.chendong.rabbitmq01.mapper.UserOrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户下单确认监听 类
 */
@Component("userOrderListener")
public class UserOrderListener implements ChannelAwareMessageListener {

    private static final Logger log = LoggerFactory.getLogger(UserOrderListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserOrderMapper userOrderMapper;
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long tag = message.getMessageProperties().getDeliveryTag();
        try{
            byte[] body = message.getBody();
            UserOrderDto entity = objectMapper.readValue(body, UserOrderDto.class);
            log.info("用户商城下单监听消息：{}",entity);
            //监听到消息后插入到数据库
            UserOrder userOrder = new UserOrder();
            BeanUtils.copyProperties(entity,userOrder);
            userOrder.setStatus(1);
            userOrderMapper.insertSelective(userOrder);
            /**
             * void basicAck(long deliveryTag, boolean multiple) throws IOException;
             * 参数解析
             * 　　deliveryTag：该消息的index
             * 　　multiple：是否批量处理.true:将一次性ack所有小于deliveryTag的消息。
             */
            channel.basicAck(tag,true);
        }catch (Exception e){
            log.info("用户商城下单 发生异常：",e.fillInStackTrace());
            channel.basicReject(tag,false);
        }
    }
}
