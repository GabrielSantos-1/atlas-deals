package com.atlasdeals.pricehistory.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "atlas-deals";
    public static final String PRICE_HISTORY_QUEUE = "price.history.q";
    public static final String PRICE_UPDATED_QUEUE = "price.updated.q";
    public static final String PRODUCT_COLLECTED_ROUTING_KEY = "product.collected";
    public static final String PRICE_UPDATED_ROUTING_KEY = "price.updated";

    @Bean
    public TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue priceHistoryQueue() {
        return new Queue(PRICE_HISTORY_QUEUE, true);
    }

    @Bean
    public Queue priceUpdatedQueue() {
        return new Queue(PRICE_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding priceHistoryBinding(TopicExchange atlasDealsExchange, Queue priceHistoryQueue) {
        return BindingBuilder.bind(priceHistoryQueue)
            .to(atlasDealsExchange)
            .with(PRODUCT_COLLECTED_ROUTING_KEY);
    }

    @Bean
    public Binding priceUpdatedBinding(TopicExchange atlasDealsExchange, Queue priceUpdatedQueue) {
        return BindingBuilder.bind(priceUpdatedQueue)
            .to(atlasDealsExchange)
            .with(PRICE_UPDATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
