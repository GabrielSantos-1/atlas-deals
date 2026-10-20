package com.atlasdeals.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Base for domain events exchanged through the message broker.
 *
 * <p>The identity fields are regular mutable properties on purpose: they must survive a
 * serialize/deserialize round trip so that consumers can use {@code eventId} as an
 * idempotency key. When an event is created locally the no-arg constructor assigns a fresh
 * identity; when an event is read from a message the values are restored from the payload.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseDomainEvent implements DomainEvent {

    @JsonProperty("eventId")
    private UUID eventId;

    @JsonProperty("occurredOn")
    private Instant occurredOn;

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
}
