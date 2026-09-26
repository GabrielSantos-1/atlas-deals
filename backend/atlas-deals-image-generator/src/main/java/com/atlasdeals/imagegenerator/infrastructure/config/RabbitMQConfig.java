package com.atlasdeals.imagegenerator.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String IMAGE_EXCHANGE = "atlasdeals.image.exchange";
    public static final String CONTENT_GENERATED_QUEUE = "content.generated.q";
    public static final String IMAGE_GENERATED_QUEUE = "image.generated.q";
    public static final String IMAGE_DLQ = "image.dlq";

    public static final String CONTENT_GENERATED_ROUTING_KEY = "content.generated";
    public static final String IMAGE_GENERATED_ROUTING_KEY = "image.generated";

    @Bean
    public DirectExchange imageExchange() {
        return new DirectExchange(IMAGE_EXCHANGE, true, false);
    }

    @Bean
    public Queue contentGeneratedQueue() {
        return new Queue(CONTENT_GENERATED_QUEUE, true, false, false);
    }

    @Bean
    public Queue imageGeneratedQueue() {
        return new Queue(IMAGE_GENERATED_QUEUE, true, false, false);
    }

    @Bean
    public Queue imageDeadLetterQueue() {
        return new Queue(IMAGE_DLQ, true, false, false);
    }

    @Bean
    public Binding contentGeneratedBinding() {
        return BindingBuilder.bind(contentGeneratedQueue())
            .to(new DirectExchange("atlasdeals.ai.exchange"))
            .with(CONTENT_GENERATED_ROUTING_KEY);
    }

    @Bean
    public Binding imageGeneratedBinding() {
        return BindingBuilder.bind(imageGeneratedQueue())
            .to(imageExchange())
            .with(IMAGE_GENERATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
