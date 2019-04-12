package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.listenter.event.PushOrderRecordEvent;
import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import com.sun.xml.internal.rngom.parse.host.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRecordController {
    private static final Logger log= LoggerFactory.getLogger(OrderRecordController.class);

    private static final String prefix = "order";
    //类似于RabbitTemplate
    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 下单
     */
    @RequestMapping(value = prefix + "/push",method = RequestMethod.GET)
    public BaseResponse pushOrder(){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try{
            PushOrderRecordEvent event = new PushOrderRecordEvent(this,"orderNo_20180821001","物流12");
            publisher.publishEvent(event);
        }catch(Exception e){
            log.error("下单发生异常：",e.fillInStackTrace());
        }
        return response;

    }


}
