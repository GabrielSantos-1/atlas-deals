package com.atlasdeals.search.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    static final String EXCHANGE = "atlas-deals";
    static final String SEARCH_INDEX_QUEUE = "search.index.q";
    static final String SEARCH_INDEX_KEY = "product.collected";

    @Bean
    TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue searchIndexQueue() {
        return new Queue(SEARCH_INDEX_QUEUE, true);
    }

    @Bean
    Binding searchIndexBinding(TopicExchange exchange, Queue searchIndexQueue) {
        return BindingBuilder.bind(searchIndexQueue).to(exchange).with(SEARCH_INDEX_KEY);
    }
}
