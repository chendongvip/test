package com.example.chendong.rabbitmq01.controller;

import com.example.chendong.rabbitmq01.entity.User;
import com.example.chendong.rabbitmq01.entity.UserLog;
import com.example.chendong.rabbitmq01.mapper.UserLogMapper;
import com.example.chendong.rabbitmq01.mapper.UserMapper;
import com.example.chendong.rabbitmq01.response.BaseResponse;
import com.example.chendong.rabbitmq01.response.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户controller类
 */
@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldController.class);

    private static final String prefix = "user";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserLogMapper userLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @RequestMapping(value = prefix + "/login",method = RequestMethod.POST,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)//提交表单
    public BaseResponse login(@RequestParam("userName")String userName,@RequestParam("password")String password
                              ){
        BaseResponse response = new BaseResponse(StatusCode.Success);
            try{
                User user = userMapper.selectByUserNamePassword(userName,password);
                if(user != null){
                    try{
                        //TODO:异步写用户日志
                    /*UserLog log = new UserLog(userName,"Login","login",objectMapper.writeValueAsString(user));
                    userLogMapper.insertSelective(log);同步
*/
                        UserLog log = new UserLog(userName,"Login","login",objectMapper.writeValueAsString(user));
                        //设置消息发送格式
                        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                        //指定交换机 路由
                        rabbitTemplate.setExchange(env.getProperty("log.user.exchange.name"));
                        rabbitTemplate.setRoutingKey(env.getProperty("log.user.routing.key.name"));

                        //建立消息 并持久化
                        Message message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(log))
                                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                                .build();
                        //发送消息
                        rabbitTemplate.convertAndSend(message);
                        //TODO:塞权限数据-资源数据-视野数据
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    response = new BaseResponse(StatusCode.Fail);
                }
            }catch (Exception e){
                    e.printStackTrace();
            }
            return response;
    }
}
