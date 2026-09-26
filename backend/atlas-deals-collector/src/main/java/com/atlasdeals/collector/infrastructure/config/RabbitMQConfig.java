package com.atlasdeals.collector.infrastructure.config;

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

    static final String EXCHANGE = "atlas-deals";
    static final String PRODUCT_COLLECTED_QUEUE = "product.collected.q";
    static final String PRODUCT_COLLECTED_KEY = "product.collected";
    static final String PRODUCT_PRICE_UPDATED_QUEUE = "product.price.updated.q";
    static final String PRODUCT_PRICE_UPDATED_KEY = "product.price.updated";

    @Bean
    TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue productCollectedQueue() {
        return new Queue(PRODUCT_COLLECTED_QUEUE, true);
    }

    @Bean
    Queue productPriceUpdatedQueue() {
        return new Queue(PRODUCT_PRICE_UPDATED_QUEUE, true);
    }

    @Bean
    Binding productCollectedBinding(TopicExchange exchange, Queue productCollectedQueue) {
        return BindingBuilder.bind(productCollectedQueue).to(exchange).with(PRODUCT_COLLECTED_KEY);
    }

    @Bean
    Binding productPriceUpdatedBinding(TopicExchange exchange, Queue productPriceUpdatedQueue) {
        return BindingBuilder.bind(productPriceUpdatedQueue).to(exchange).with(PRODUCT_PRICE_UPDATED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
