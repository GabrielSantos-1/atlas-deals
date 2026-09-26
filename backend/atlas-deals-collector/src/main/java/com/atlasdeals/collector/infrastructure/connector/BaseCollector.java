package com.atlasdeals.collector.infrastructure.connector;

import com.atlasdeals.collector.application.dto.CollectionResult;
import com.atlasdeals.collector.application.dto.ConnectorConfig;
import com.atlasdeals.collector.application.port.ProductCollector;
import com.atlasdeals.collector.domain.model.RawProduct;
import com.atlasdeals.collector.domain.repository.RawProductRepository;
import com.atlasdeals.collector.infrastructure.messaging.ProductEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCollector implements ProductCollector {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ObjectMapper objectMapper;
    protected final RawProductRepository repository;
    protected final ProductEventPublisher eventPublisher;
    protected final TokenBucket rateLimiter;
    protected final RetryHandler retryHandler;
    protected final String storeName;

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final long MAX_RETRY_DELAY_MS = 30000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    protected BaseCollector(String storeName, int maxRequestsPerMinute,
                            ObjectMapper objectMapper,
                            RawProductRepository repository,
                            ProductEventPublisher eventPublisher) {
        this.storeName = storeName;
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.rateLimiter = new TokenBucket(maxRequestsPerMinute, maxRequestsPerMinute, Duration.ofMinutes(1));
        this.retryHandler = new RetryHandler(DEFAULT_MAX_RETRIES, INITIAL_RETRY_DELAY_MS,
            MAX_RETRY_DELAY_MS, BACKOFF_MULTIPLIER);
    }

    @Override
    public boolean supports(String storeName) {
        return this.storeName.equalsIgnoreCase(storeName);
    }

    @Override
    public CollectionResult collect() {
        Instant start = Instant.now();
        try {
            if (!rateLimiter.tryConsume()) {
                log.warn("Rate limit exceeded for {}. Waiting before retry.", storeName);
                Thread.sleep(1000 / rateLimiter.capacity * 2);
                if (!rateLimiter.tryConsume()) {
                    return CollectionResult.failure(storeName, 1, Duration.between(start, Instant.now()));
                }
            }
            CollectionResult result = retryHandler.execute(this::doCollect);
            log.info("Collection completed for {}: {} new, {} updated, {} errors",
                result.storeName(), result.itemsNew(), result.itemsUpdated(), result.errors());
            return result;
        } catch (Exception e) {
            log.error("Collection failed for {}: {}", storeName, e.getMessage(), e);
            return CollectionResult.failure(storeName, 1, Duration.between(start, Instant.now()));
        }
    }

    protected abstract CollectionResult doCollect();

    protected boolean isDuplicate(RawProduct product) {
        if (product.getExternalId() != null && product.getStoreName() != null) {
            Optional<RawProduct> existing = repository.findByExternalIdAndStoreName(
                product.getExternalId(), product.getStoreName());
            if (existing.isPresent()) {
                return true;
            }
        }
        if (product.getProductUrl() != null) {
            int hash = product.getProductUrl().hashCode();
            Optional<RawProduct> existing = repository.findByProductUrlHash(hash);
            if (existing.isPresent()) {
                return true;
            }
        }
        return false;
    }

    protected RawProduct saveProduct(RawProduct product) {
        return repository.save(product);
    }

    protected JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    protected String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    protected String generateId() {
        return java.util.UUID.randomUUID().toString();
    }

    protected static class TokenBucket {
        private final long capacity;
        private final long refillTokens;
        private final Duration refillDuration;
        private long tokens;
        private Instant lastRefill;

        public TokenBucket(long capacity, long refillTokens, Duration refillDuration) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillDuration = refillDuration;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            long elapsed = Duration.between(lastRefill, now).toMillis();
            long periodMs = refillDuration.toMillis();
            if (periodMs <= 0) return;
            long periods = elapsed / periodMs;
            if (periods > 0) {
                tokens = Math.min(capacity, tokens + periods * refillTokens);
                lastRefill = lastRefill.plus(refillDuration.multipliedBy(periods));
            }
        }
    }

    protected static class RetryHandler {
        private final int maxRetries;
        private final long initialDelayMs;
        private final long maxDelayMs;
        private final double multiplier;

        public RetryHandler(int maxRetries, long initialDelayMs, long maxDelayMs, double multiplier) {
            this.maxRetries = maxRetries;
            this.initialDelayMs = initialDelayMs;
            this.maxDelayMs = maxDelayMs;
            this.multiplier = multiplier;
        }

        public <T> T execute(Supplier<T> operation) {
            int attempt = 0;
            long delay = initialDelayMs;
            while (true) {
                try {
                    return operation.get();
                } catch (Exception e) {
                    attempt++;
                    if (attempt >= maxRetries) {
                        throw new RuntimeException("Operation failed after " + maxRetries + " retries", e);
                    }
                    long jitter = ThreadLocalRandom.current().nextLong(delay / 2);
                    try {
                        Thread.sleep(delay + jitter);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                    delay = Math.min((long) (delay * multiplier), maxDelayMs);
                }
            }
        }
    }
}
