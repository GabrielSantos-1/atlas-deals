package com.atlasdeals.notification.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    static final String EXCHANGE = "atlas-deals";
    static final String NOTIFICATION_QUEUE = "notification.q";
    static final String NOTIFICATION_ROUTING_KEY = "notification.#";

    @Bean
    TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    Binding notificationBinding(TopicExchange exchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue).to(exchange).with(NOTIFICATION_ROUTING_KEY);
    }
}
