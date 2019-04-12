package com.example.chendong.rabbitmq01.mapper;

import com.example.chendong.rabbitmq01.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.messaging.handler.annotation.Payload;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByUserNamePassword(@Param("userName") String userName, @Param("password") String password);
}