package com.order.service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "orders.exchange";
    public static final String ORDER_QUEUE = "orders.queue";
    public static final String ORDER_ROUTING_KEY = "order.created";
    public static final String ORDER_DLX = "orders.dlx";
    public static final String ORDER_DL_QUEUE = "orders.dead-letter-queue";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DLX)
                .withArgument("x-dead-letter-routing-key", "order.dead")
                .build();
    }

    @Bean
    public Binding orderBinding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(orderQueue)
                .to(orderExchange)
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public DirectExchange orderDlx() {
        return new DirectExchange(ORDER_DLX, true, false);
    }

    @Bean
    public Queue orderDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_DL_QUEUE).build();
    }

    @Bean
    public Binding orderDlBinding(Queue orderDeadLetterQueue, DirectExchange orderDlx) {
        return BindingBuilder
                .bind(orderDeadLetterQueue)
                .to(orderDlx)
                .with("order.dead");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
}