package com.atlasdeals.publisher.infrastructure.publisher;

import com.atlasdeals.publisher.application.dto.PublishRequest;
import com.atlasdeals.publisher.application.dto.PublishResult;
import com.atlasdeals.publisher.application.port.Publisher;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BasePublisher implements Publisher {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final MeterRegistry meterRegistry;
    protected final String platform;
    protected final Semaphore rateLimiter;
    protected final AtomicLong lastRequestTime = new AtomicLong(0);
    protected final long minIntervalMs;
    protected final int maxRetries;
    protected final long retryBackoffMs;

    protected BasePublisher(String platform, MeterRegistry meterRegistry,
                            int maxRequestsPerSecond, int maxRetries, long retryBackoffMs) {
        this.platform = platform;
        this.meterRegistry = meterRegistry;
        this.rateLimiter = new Semaphore(maxRequestsPerSecond, true);
        this.minIntervalMs = 1000 / maxRequestsPerSecond;
        this.maxRetries = maxRetries;
        this.retryBackoffMs = retryBackoffMs;
    }

    @Override
    public PublishResult publish(PublishRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        Instant start = Instant.now();

        try {
            acquireRateLimit();

            PublishResult result = doPublish(request);

            sample.stop(Timer.builder("publisher.publish.duration")
                .tag("platform", platform)
                .tag("success", String.valueOf(result.success()))
                .register(meterRegistry));

            meterRegistry.counter("publisher.publish.total",
                "platform", platform,
                "result", result.success() ? "success" : "failure"
            ).increment();

            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder("publisher.publish.duration")
                .tag("platform", platform)
                .tag("success", "false")
                .register(meterRegistry));

            meterRegistry.counter("publisher.publish.total",
                "platform", platform,
                "result", "failure"
            ).increment();

            log.error("Error publishing to {}: {}", platform, e.getMessage(), e);
            return new PublishResult(false, null, e.getMessage(), Instant.now());
        }
    }

    protected abstract PublishResult doPublish(PublishRequest request);

    protected void acquireRateLimit() {
        long now = System.currentTimeMillis();
        long last = lastRequestTime.get();
        long elapsed = now - last;

        if (elapsed < minIntervalMs) {
            try {
                Thread.sleep(minIntervalMs - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            rateLimiter.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        lastRequestTime.set(System.currentTimeMillis());
        rateLimiter.release();
    }

    protected PublishResult retryWithBackoff(PublishRequest request, int retries) {
        Exception lastException = null;

        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                if (attempt > 0) {
                    long backoff = retryBackoffMs * (long) Math.pow(2, attempt - 1);
                    log.info("Retrying publish to {} after {}ms (attempt {}/{})",
                        platform, backoff, attempt + 1, retries + 1);
                    Thread.sleep(backoff);
                }

                return doPublish(request);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new PublishResult(false, null, "Interrupted during retry", Instant.now());
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} to publish to {} failed: {}",
                    attempt + 1, retries + 1, platform, e.getMessage());
            }
        }

        return new PublishResult(false, null,
            "All retries exhausted: " + lastException.getMessage(), Instant.now());
    }

    @Override
    public String getPlatform() {
        return platform;
    }
}
