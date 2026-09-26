package com.atlasdeals.promotionengine.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "atlas-deals";
    public static final String PROMOTION_ENGINE_QUEUE = "promotion.engine.q";
    public static final String PROMOTION_DETECTED_QUEUE = "promotion.detected.q";
    public static final String PRODUCT_COLLECTED_ROUTING_KEY = "product.collected";
    public static final String PRICE_UPDATED_ROUTING_KEY = "price.updated";
    public static final String PROMOTION_DETECTED_ROUTING_KEY = "promotion.detected";

    @Bean
    public TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue promotionEngineQueue() {
        return new Queue(PROMOTION_ENGINE_QUEUE, true);
    }

    @Bean
    public Queue promotionDetectedQueue() {
        return new Queue(PROMOTION_DETECTED_QUEUE, true);
    }

    @Bean
    public Binding productCollectedBinding(TopicExchange atlasDealsExchange, Queue promotionEngineQueue) {
        return BindingBuilder.bind(promotionEngineQueue)
            .to(atlasDealsExchange)
            .with(PRODUCT_COLLECTED_ROUTING_KEY);
    }

    @Bean
    public Binding priceUpdatedBinding(TopicExchange atlasDealsExchange, Queue promotionEngineQueue) {
        return BindingBuilder.bind(promotionEngineQueue)
            .to(atlasDealsExchange)
            .with(PRICE_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding promotionDetectedBinding(TopicExchange atlasDealsExchange, Queue promotionDetectedQueue) {
        return BindingBuilder.bind(promotionDetectedQueue)
            .to(atlasDealsExchange)
            .with(PROMOTION_DETECTED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
