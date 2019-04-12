package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.entity.OrderRecord;
import com.example.chendong.rabbitmq01.mapper.OrderRecordMapper;
import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import com.sun.xml.internal.rngom.parse.host.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldController.class);

    private static final String prefix = "helloWorld";

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    /**
     * 测试SpringBoot整合是否有问题  --helloworld
     */
    @RequestMapping(value = prefix + "/rabbitmq",method = RequestMethod.GET)
    public BaseResponse rabbitmq(){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        String str = "rabbitmq的第二阶段 spring boot 整合rabbitmq";
        response.setData(str);
        return response;
    }

    /**
     * 整合mybatis访问数据列表
     */
    @RequestMapping(value = prefix + "/data/list",method = RequestMethod.GET)
    public BaseResponse dataList(){
        BaseResponse response = new BaseResponse(StatusCode.Success);
        response.setData(orderRecordMapper.selectAll());
        return response;
    }

}
