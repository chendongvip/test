package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import com.example.chendong.rabbitmq01.service.InitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConcurrencyController {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldController.class);

    private static final String prefix = "concurrency";

    @Autowired
    private InitService initService;

    @RequestMapping(value = prefix + "robbing/thread" , method = RequestMethod.GET)
    public BaseResponse robbingThread(){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        //initService.generteMultiThread();
        return response;
    }

}
