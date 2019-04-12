package com.example.chendong.rabbitmq01.rabbitmq;

import com.example.chendong.rabbitmq01.dto.LogDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


/**
 * 日志监听类
 */
@Component
public class LogSystemListener {

    private static final Logger log = LoggerFactory.getLogger(LogSystemListener.class);

    @RabbitListener(queues = "${log.system.queue.name}",containerFactory = "multiListenerContainer")
    public void consumeLogInfo(@Payload LogDto logDto){
        try{
            log.info("系统日志监听器监听到消息：{}",logDto);
        }catch (Exception e){
            log.error("系统日志监听器监听发生的异常：{}",logDto,e.fillInStackTrace());
        }

    }


}
