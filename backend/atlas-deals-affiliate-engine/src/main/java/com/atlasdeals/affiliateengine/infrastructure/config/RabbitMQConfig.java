package com.atlasdeals.affiliateengine.infrastructure.config;

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
    public static final String AFFILIATE_LINK_GENERATED_ROUTING_KEY = "affiliate.link.generated";
    public static final String AFFILIATE_ENGINE_QUEUE = "affiliate.engine.q";

    @Bean
    public TopicExchange atlasDealsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue affiliateEngineQueue() {
        return new Queue(AFFILIATE_ENGINE_QUEUE, true);
    }

    @Bean
    public Binding affiliateLinkGeneratedBinding(TopicExchange atlasDealsExchange, Queue affiliateEngineQueue) {
        return BindingBuilder.bind(affiliateEngineQueue)
            .to(atlasDealsExchange)
            .with(AFFILIATE_LINK_GENERATED_ROUTING_KEY);
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
