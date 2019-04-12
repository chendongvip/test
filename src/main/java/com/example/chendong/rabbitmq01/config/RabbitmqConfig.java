package com.example.chendong.rabbitmq01.config;


import com.example.chendong.rabbitmq01.rabbitmq.SimpleListener;
import com.example.chendong.rabbitmq01.rabbitmq.UserOrderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * rabbitmq 全局配置类
 */
@Configuration
public class RabbitmqConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqConfig.class);

    @Autowired
    private Environment env;

    @Autowired
    //连接工厂
    private CachingConnectionFactory connectionFactory;

    @Autowired
    /**
     * 果你需要创建更多的RabbitListenerContainerFactory实例，
     * 或者你想要覆盖缺省值，Spring Boot提供了一个SimpleRabbitListenerContainerFactoryConfigurer
     * 和一个DirectRabbitListenerContainerFactoryConfigurer，
     * 你可以使用它们初始化一个SimpleRabbitListenerContainerFactory，
     * 以及一个DirectRabbitListenerContainerFactory，
     * 其设置与自动配置使用的工厂相同。
     */

    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * 单一消费者
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(){
        //监听工厂SimpleRabbitListenerContainerFactory
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //监听工厂连接 连接池
        factory.setConnectionFactory(connectionFactory);
        //设置消息传输格式 json
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //设置线程并发
        //创建最低消费人数
        factory.setConcurrentConsumers(1);
        //创建最高消费人数
        factory.setMaxConcurrentConsumers(1);
        //预取计数 实现更加均匀的任务分发。
        factory.setPrefetchCount(1);
        //设置事务当中可以处理的消息数量。
        factory.setTxSize(1);
        return factory;
    }

    /**
     * 多个消费者
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer(){
        //创建监听工厂SimpleRabbitListenerContainerFactory
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //配置指定的rabbitmq监听容器工厂。
        factoryConfigurer.configure(factory,connectionFactory);
        //设置数据传输格式 json
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //消息确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        //设置最低消费人数
        factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",int.class));
        //设置最高消费人数
        factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",int.class));
        //预取计数 实现更加均匀的任务分发。
        factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",int.class));
        return factory;
    }

    /**
     * 发送消费的组件
     */
    @Bean
    public RabbitTemplate rabbitTemplate(){
        //发布确认必须配置在CachingConnectionFactory上
        //若使用confirm-callback或return-callback，必须要配置publisherConfirms或publisherReturns为true
        //每个rabbitTemplate只能有一个confirm-callback和return-callback
        //如果启用发布服务器确认，则返回true。
        connectionFactory.setPublisherConfirms(true);
        //如果启用发行者返回，则返回true。
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        /**
         * confirm模式下,broker将会确认消息并处理。这种模式下是异步的，生产者可以流水式的发布而不用等待broker，broker可以批量的往磁盘写入。
         * 确认消息是否到达broker服务器，也就是只确认是否正确到达exchange中即可，
         * 只要正确的到达exchange中，broker即可确认该消息返回给客户端ack。
         * 使用return-callback时必须设置mandatory为true，或者在配置中设置mandatory-expression的值为true，
         * 可针对每次请求的消息去确定’mandatory’的boolean值，只能在提供’return -callback’时使用，与mandatory互斥。
         */
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                log.info("消息发送成功：correlationDate(),b({}),cause({})",correlationData,b,s);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int i, String s, String s1, String s2) {
                log.info("消息丢失：s1（{}）,s2({}),i({}),s({}),message:(}",s1,s2,i,s,message);
            }
        });
        return rabbitTemplate;
    }

    //TODO:基本消息模型构建
    @Bean
    public DirectExchange basicExchange(){
        return new DirectExchange(env.getProperty("basic.info.mq.exchange.name"),true,false);
    }

    @Bean(name = "basicQueue")
    public Queue basicQueue(){
        return new Queue(env.getProperty("basic.info.mq.queue.name"),true);
    }

    @Bean
    public Binding basicBinding(){
        return  BindingBuilder.bind(basicQueue()).to(basicExchange()).with(env.getProperty("basic.info.mq.routing.key.name"));
    }

    //TODO:s商品抢单信息模型构建
    @Bean
    public DirectExchange robbingExchange(){
        return new DirectExchange(env.getProperty("product.robbing.mq.exchange.name"),true,false);
    }

    @Bean(name = "robbingQueue")
    public Queue robbingQueue(){
        return new Queue(env.getProperty("product.robbing.mq.queue.name"),true);
    }

    @Bean
    public Binding robbingBinding(){
        return  BindingBuilder.bind(robbingQueue()).to(robbingExchange()).with(env.getProperty("product.robbing.mq.routing.key.name"));
    }

    //TODO:消息确认机制以及并发配置-listener (消费者)
    //=================================================================================基本消息模型构建
    //配置消息队列
    @Bean(name = "simpleQueue")
    public Queue simpleQueue(){
        return new Queue(env.getProperty("simple.mq.queue.name"),true);
    }
    //配置topic模式交换机
    @Bean
    public TopicExchange simpleExchange(){
        return new TopicExchange(env.getProperty("simple.mq.exchange.name"),true,false);
    }
    @Bean
    public Binding simpleBinding(){
        return  BindingBuilder.bind(simpleQueue()).to(simpleExchange()).with(env.getProperty("simple.mq.routing.key.name"));
    }
    //=================================================================================基本消息模型构建

    @Autowired
    private SimpleListener simpleListener;

    @Bean(name = "simpleContainer")
    public SimpleMessageListenerContainer simpleContainer(@Qualifier("simpleQueue") Queue simpleQueue){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //设置消息传输格式 json
        container.setMessageConverter(new Jackson2JsonMessageConverter());
        //设置最低消费人数
        container.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",Integer.class));
        //设置最高消费人数
        container.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",Integer.class));
        //预取计数 实现更加均匀的任务分发。
        container.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",Integer.class));
        //TODO: 消息确认 - 确认机制种类
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setQueues(simpleQueue);
        container.setMessageListener(simpleListener);

        return container;
    }

    //TODO:消息确认机制以及并发配置-listener (消费者)


    //TODO:用户商城下单实战
    //配置消息队列
    @Bean(name="userOrderQueue")
    public Queue userOrderQueue(){
        return new Queue(env.getProperty("user.order.queue.name"),true);
    }
    //配置topic模式交换机
    @Bean
    public TopicExchange userOrderExchange(){
        return new TopicExchange(env.getProperty("user.order.exchange.name"),true,false);
    }
    @Bean
    public Binding userOrderBinding(){
        return  BindingBuilder.bind(userOrderQueue()).to(userOrderExchange()).with(env.getProperty("user.order.routing.key.name"));
    }
    @Autowired
    private UserOrderListener userOrderListener;

    @Bean
    public SimpleMessageListenerContainer listenerContainerUserOrder(@Qualifier("userOrderQueue")Queue userOrderQueue){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.setMessageConverter(new Jackson2JsonMessageConverter());
        container.setQueues(userOrderQueue);
        container.setMessageListener(userOrderListener);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        return container;
    }
    //TODO:用户商城下单实战



    //TODO:日志消息模型
    //配置消息队列
    @Bean(name="logSystemQueue")
    public Queue logSystemQueue(){
        return new Queue(env.getProperty("log.system.queue.name"),true);
    }
    //配置topic模式交换机
    @Bean
    public TopicExchange logSystemExchange(){
        return new TopicExchange(env.getProperty("log.system.exchange.name"),true,false);
    }
    @Bean
    public Binding logSystemBinding(){
        return  BindingBuilder.bind(logSystemQueue()).to(logSystemExchange()).with(env.getProperty("log.system.routing.key.name"));
    }
    //TODO:日志消息模型


    //TODO:用户日志消息模型
    //配置消息队列
    @Bean(name="logUserQueue")
    public Queue logUserQueue(){
        return new Queue(env.getProperty("log.user.queue.name"),true);
    }
    //配置topic模式交换机
    @Bean
    public DirectExchange logUserExchange(){
        return new DirectExchange(env.getProperty("log.user.exchange.name"),true,false);
    }
    @Bean
    public Binding logUserBinding(){
        return  BindingBuilder.bind(logUserQueue()).to(logUserExchange()).with(env.getProperty("log.user.routing.key.name"));
    }
    //TODO:用户日志消息模型

    //TODO：发送邮件消息模型

    @Bean
    public Queue mailQueue(){
        return new Queue(env.getProperty("mail.queue.name"),true);
    }

    @Bean
    public DirectExchange mailExchange(){
        return new DirectExchange(env.getProperty("mail.exchange.name"),true,false);
    }

    @Bean
    public Binding mailBinding(){
        return BindingBuilder.bind(mailQueue()).to(mailExchange()).with(env.getProperty("mail.routing.key.name"));
    }
    //TODO：发送邮件消息模型


    //TODO:死信队列消息模型
    //创建死信队列
    @Bean
    public Queue simpleDeadQueue(){
        Map<String,Object> args = new HashMap();
        args.put("x-dead-letter-exchange",env.getProperty("simple.dead.exchange.name"));
        args.put("x-dead-letter-routing-key",env.getProperty("simple.dead.routing.key.name"));
        args.put("x-message-ttl",10000);

        return new Queue(env.getProperty("simple.dead.queue.name"),true,false,false,args);
    }
    //绑定死信队列--面向生产端
    @Bean
    public TopicExchange simpleDeadExchange(){
        return new TopicExchange(env.getProperty("simple.produce.exchange.name"),true,false);
    }

    @Bean
    public Binding simpleDeadBinding(){
        return BindingBuilder.bind(simpleDeadQueue()).to(simpleDeadExchange()).with(env.getProperty("simple.produce.routing.key.name"));
    }

    //创建并绑定实际监听消息队列
    @Bean
    public Queue simpleDeadRealQueue(){
        return new Queue(env.getProperty("simple.dead.real.queue.name"),true);
    }

    @Bean
    public TopicExchange simpleDeadRealExchange(){
        return new TopicExchange(env.getProperty("simple.dead.exchange.name"),true,false);
    }

    @Bean
    public Binding simpleDeadRealBinding(){
        return BindingBuilder.bind(simpleDeadRealQueue()).to(simpleDeadRealExchange()).with(env.getProperty("simple.dead.routing.key.name"));
    }
    //TODO:死信队列消息模型

    //TODO:用户下单支付超时死信队列模型
    //创建死信队列
    @Bean
    public Queue userOrderDeadQueue(){
        Map<String, Object> args=new HashMap();
        args.put("x-dead-letter-exchange",env.getProperty("user.order.dead.exchange.name"));
        args.put("x-dead-letter-routing-key",env.getProperty("user.order.dead.routing.key.name"));
        args.put("x-message-ttl",10000);

        return new Queue(env.getProperty("user.order.dead.queue.name"),true,false,false,args);
    }
    //绑定死信队列--面向生产端
    @Bean
    public TopicExchange userOrderDeadExchange(){
        return new TopicExchange(env.getProperty("user.order.dead.produce.exchange.name"),true,false);
    }

    @Bean
    public Binding userOrderDeadBinding(){
        return BindingBuilder.bind(userOrderDeadQueue()).to(userOrderDeadExchange()).with(env.getProperty("user.order.dead.produce.routing.key.name"));
    }

    //创建并绑定实际监听消息队列
    @Bean
    public Queue userOrderDeadRealQueue(){
        return new Queue(env.getProperty("user.order.dead.real.queue.name"),true);
    }

    @Bean
    public TopicExchange userOrderDeadRealExchange(){
        return new TopicExchange(env.getProperty("user.order.dead.exchange.name"));
    }

    @Bean
    public Binding userOrderDeadRealBinding(){
        return BindingBuilder.bind(userOrderDeadRealQueue()).to(userOrderDeadRealExchange()).with(env.getProperty("user.order.dead.routing.key.name"));
    }
    //TODO:用户下单支付超时死信队列模型


    //TODO：死信队列动态设置TTL消息模型
    //创建死信队列
    @Bean
    public Queue dynamicDeadQueue(){
        Map<String, Object> args=new HashMap();
        args.put("x-dead-letter-exchange",env.getProperty("dynamic.dead.exchange.name"));
        args.put("x-dead-letter-routing-key",env.getProperty("dynamic.dead.routing.key.name"));

        return new Queue(env.getProperty("dynamic.dead.queue.name"),true,false,false,args);
    }
    //绑定死信队列--面向生产端
    @Bean
    public TopicExchange dynamicDeadExchange(){
        return new TopicExchange(env.getProperty("dynamic.dead.produce.exchange.name"),true,false);
    }

    @Bean
    public Binding dynamicDeadBinding(){
        return BindingBuilder.bind(dynamicDeadQueue()).to(dynamicDeadExchange()).with(env.getProperty("dynamic.dead.produce.routing.key.name"));
    }

    //创建并绑定实际监听消息队列
    @Bean
    public Queue dynamicDeadRealQueue(){
        return new Queue(env.getProperty("dynamic.dead.real.queue.name"),true);
    }


    @Bean
    public TopicExchange dynamicDeadRealExchange(){
        return new TopicExchange(env.getProperty("dynamic.dead.exchange.name"));
    }

    @Bean
    public Binding dynamicDeadRealBinding(){
        return BindingBuilder.bind(dynamicDeadRealQueue()).to(dynamicDeadRealExchange()).with(env.getProperty("dynamic.dead.routing.key.name"));
    }
    //TODO：死信队列动态设置TTL消息模型

}