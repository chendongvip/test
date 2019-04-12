package com.example.chendong.rabbitmq01.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;


@Service
public class InitService {

    private static final Logger log = LoggerFactory.getLogger(InitService.class);

    @Autowired
    private ConcurrencyService concurrencyService;
    @Autowired
    //消息中间件
    private CommonMqService commonMqService;
    //设置线程大小
    public static final int ThreadNum = 500;

    private static int mobile = 0;

/*
    @PostConstruct
    public void generteMultiThread(){
        log.info("开始初始化线程数 - - - - - - >");

        try{
            CountDownLatch countDownLatch = new CountDownLatch(1);
            for(int i=0;i<ThreadNum;i++){
                new Thread(new RunThread(countDownLatch)).start();
            }

            //TODO:启动多个线程
            countDownLatch.countDown();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
*/

    /*private class RunThread implements Runnable{
        private final CountDownLatch starLatch;

        public RunThread(CountDownLatch starLatch){
            this.starLatch = starLatch;
        }

        public void run(){
            try{
                //:TODO:线程等待
                starLatch.await();

                mobile += 1;
                //log.info("当前手机号：{}",mobile);

                *//*concurrencyService.manageRobbing(String.valueOf(mobile));v-1*//*
                //commonMqService.sendRobbingMsg(String.valueOf(mobile));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }*/
}
