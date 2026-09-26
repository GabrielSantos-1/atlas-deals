package com.atlasdeals.aiengine.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    public static final String MAIN_EXCHANGE = "atlas-deals";
    public static final String PROMOTION_DETECTED_QUEUE = "ai.promotion.detected.q";
    public static final String CONTENT_GENERATED_QUEUE = "content.generated.q";
    public static final String AI_EXCHANGE = "atlasdeals.ai.exchange";
    public static final String AI_DLQ = "ai.dlq";

    public static final String PROMOTION_DETECTED_ROUTING_KEY = "promotion.detected";
    public static final String CONTENT_GENERATED_ROUTING_KEY = "content.generated";

    @Bean
    public TopicExchange atlasDealsExchange() {
        return new TopicExchange(MAIN_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange aiExchange() {
        return new DirectExchange(AI_EXCHANGE, true, false);
    }

    @Bean
    public Queue promotionDetectedQueue() {
        return new Queue(PROMOTION_DETECTED_QUEUE, true, false, false);
    }

    @Bean
    public Queue contentGeneratedQueue() {
        return new Queue(CONTENT_GENERATED_QUEUE, true, false, false);
    }

    @Bean
    public Queue aiDeadLetterQueue() {
        return new Queue(AI_DLQ, true, false, false);
    }

    @Bean
    public Binding promotionDetectedBinding() {
        return BindingBuilder.bind(promotionDetectedQueue())
            .to(atlasDealsExchange())
            .with(PROMOTION_DETECTED_ROUTING_KEY);
    }

    @Bean
    public Binding contentGeneratedBinding() {
        return BindingBuilder.bind(contentGeneratedQueue())
            .to(aiExchange())
            .with(CONTENT_GENERATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
