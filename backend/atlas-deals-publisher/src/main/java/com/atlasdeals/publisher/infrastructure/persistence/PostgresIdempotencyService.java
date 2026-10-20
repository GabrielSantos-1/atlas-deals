package com.atlasdeals.publisher.infrastructure.persistence;

import com.atlasdeals.common.application.port.IdempotencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Postgres-backed idempotency barrier. The insert is the check: a single
 * {@code INSERT ... ON CONFLICT DO NOTHING} decides the winner, so concurrent deliveries of
 * the same event cannot both be considered new.
 */
@Service
public class PostgresIdempotencyService implements IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public PostgresIdempotencyService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean tryMarkProcessed(String eventId, String consumer) {
        return processedEventRepository.insertIfAbsent(eventId, consumer) == 1;
    }
}
