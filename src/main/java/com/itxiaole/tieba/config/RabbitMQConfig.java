package com.itxiaole.tieba.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. 声明队列名字
    public static final String AUDIT_QUEUE = "post.audit.queue";
    // 2. 声明交换机名字
    public static final String POST_EXCHANGE = "post.topic.exchange";
    // 3. 声明路由键 RoutingKey
    public static final String AUDIT_ROUTING_KEY = "post.audit";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true); // true 代表持久化，服务重启队列不丢失
    }

    @Bean
    public TopicExchange postExchange() {
        return new TopicExchange(POST_EXCHANGE);
    }

    @Bean
    public Binding bindingAudit(Queue auditQueue, TopicExchange postExchange) {
        // 将队列和交换机绑定
        return BindingBuilder.bind(auditQueue).to(postExchange).with(AUDIT_ROUTING_KEY);
    }
}