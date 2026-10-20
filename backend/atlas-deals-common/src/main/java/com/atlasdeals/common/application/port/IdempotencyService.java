package com.atlasdeals.common.application.port;

/**
 * Tracks which domain events have already been processed by a given consumer.
 *
 * <p>Implementations must make the "check and mark" operation a single atomic step, so two
 * concurrent deliveries of the same event can never both be treated as new.</p>
 */
public interface IdempotencyService {

    /**
     * Atomically marks an event as processed for a consumer.
     *
     * <p>This method <strong>must be called inside the same transaction</strong> as the
     * business effect it guards. That way the marker and the effect commit (or roll back)
     * together: if the process dies after the commit but before the broker ack, the
     * redelivery is recognized as already processed.</p>
     *
     * @param eventId  stable identity of the domain event
     * @param consumer logical name of the consumer, used for auditing
     * @return {@code true} if this call marked the key now; {@code false} if it was already processed
     */
    boolean tryMarkProcessed(String eventId, String consumer);
}
