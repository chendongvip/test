package com.example.chendong.rabbitmq01.listenter;

import com.example.chendong.rabbitmq01.entity.OrderRecord;
import com.example.chendong.rabbitmq01.listenter.event.PushOrderRecordEvent;
import com.example.chendong.rabbitmq01.mapper.OrderRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 这就是监听器 跟RabbitMQd的listener几乎是一个理念
 */
@Component
public class PushOrderRecordListener implements ApplicationListener<PushOrderRecordEvent> {

    private static final Logger log = LoggerFactory.getLogger(PushOrderRecordListener.class);

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Override
    public void onApplicationEvent(PushOrderRecordEvent pushOrderRecordEvent) {
        log.info("监听到的下单记录，{}",pushOrderRecordEvent);

        try{
            if(pushOrderRecordEvent != null){
                OrderRecord entity = new OrderRecord();
                BeanUtils.copyProperties(pushOrderRecordEvent,entity);
                orderRecordMapper.insertSelective(entity);
            }
        }catch (Exception e){
             log.error("监听下单记录发生异常，{}",pushOrderRecordEvent,e.fillInStackTrace());
        }
    }
}
