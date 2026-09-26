package com.atlasdeals.common.domain;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    Instant getOccurredOn();
    String getEventType();
}
