package com.atlasdeals.common.application.port;

import com.atlasdeals.common.domain.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
    void publish(String routingKey, DomainEvent event);
}
