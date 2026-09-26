package com.atlasdeals.analytics.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    static final String EXCHANGE = "atlas-deals";
    static final String ANALYTICS_CLICK_QUEUE = "analytics.click.q";
    static final String ANALYTICS_CLICK_KEY = "analytics.click";
    static final String ANALYTICS_CONVERSION_QUEUE = "analytics.conversion.q";
    static final String ANALYTICS_CONVERSION_KEY = "analytics.conversion";

    @Bean
    TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue analyticsClickQueue() {
        return new Queue(ANALYTICS_CLICK_QUEUE, true);
    }

    @Bean
    Queue analyticsConversionQueue() {
        return new Queue(ANALYTICS_CONVERSION_QUEUE, true);
    }

    @Bean
    Binding analyticsClickBinding(TopicExchange exchange, Queue analyticsClickQueue) {
        return BindingBuilder.bind(analyticsClickQueue).to(exchange).with(ANALYTICS_CLICK_KEY);
    }

    @Bean
    Binding analyticsConversionBinding(TopicExchange exchange, Queue analyticsConversionQueue) {
        return BindingBuilder.bind(analyticsConversionQueue).to(exchange).with(ANALYTICS_CONVERSION_KEY);
    }
}
