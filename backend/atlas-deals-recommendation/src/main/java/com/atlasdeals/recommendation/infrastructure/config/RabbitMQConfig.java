package com.atlasdeals.recommendation.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    static final String EXCHANGE = "atlas-deals";
    static final String RECOMMENDATION_QUEUE = "recommendation.q";
    static final String RECOMMENDATION_KEY = "analytics.#";

    @Bean
    TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue recommendationQueue() {
        return new Queue(RECOMMENDATION_QUEUE, true);
    }

    @Bean
    Binding recommendationBinding(TopicExchange exchange, Queue recommendationQueue) {
        return BindingBuilder.bind(recommendationQueue).to(exchange).with(RECOMMENDATION_KEY);
    }
}
