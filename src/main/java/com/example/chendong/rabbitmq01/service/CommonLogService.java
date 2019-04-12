package com.example.chendong.rabbitmq01.service;

import com.example.chendong.rabbitmq01.dto.LogDto;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 公共日志service
 */
@Service
public class CommonLogService {

    private static final Logger log = LoggerFactory.getLogger(CommonLogService.class);

    /**
     * 通用的写日志服务逻辑
     */
    public void insertLog(LogDto dto){
        log.info("通用的写日志服务逻辑 数据：{}",dto);
    }

}
