package com.atlasdeals.publisher.infrastructure.publisher;

import com.atlasdeals.publisher.application.dto.PublishRequest;
import com.atlasdeals.publisher.application.dto.PublishResult;
import com.atlasdeals.publisher.infrastructure.config.PublisherConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenericWebhookPublisher extends BasePublisher {

    private static final Logger log = LoggerFactory.getLogger(GenericWebhookPublisher.class);
    private static final int MAX_REQUESTS_PER_SECOND = 10;

    private final RestTemplate restTemplate;
    private final String webhookUrl;
    private final String apiKey;
    private final boolean useWebhook;

    public GenericWebhookPublisher(String platform,
                                   PublisherConfig.PlatformConfig config,
                                   MeterRegistry meterRegistry,
                                   RestTemplateBuilder restTemplateBuilder,
                                   int maxRetries,
                                   long retryBackoffMs) {
        super(platform, meterRegistry, MAX_REQUESTS_PER_SECOND, maxRetries, retryBackoffMs);
        this.webhookUrl = config.getWebhookUrl();
        this.apiKey = config.getApiKey();
        this.useWebhook = webhookUrl != null && !webhookUrl.isBlank();
        this.restTemplate = restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected PublishResult doPublish(PublishRequest request) {
        String text = request.content();
        if (text == null || text.isBlank()) {
            return new PublishResult(false, null, "Content is empty", Instant.now());
        }

        if (useWebhook) {
            return publishViaWebhook(request);
        } else {
            return logOnly(request);
        }
    }

    private PublishResult publishViaWebhook(PublishRequest request) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("platform", platform);
            payload.put("productId", request.productId());
            payload.put("content", request.content());
            payload.put("imageUrl", request.imageUrl());
            payload.put("affiliateUrl", request.affiliateUrl());
            payload.put("timestamp", Instant.now().toString());

            if (request.metadata() != null) {
                payload.put("metadata", request.metadata());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isBlank()) {
                headers.setBearerAuth(apiKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, entity, String.class);

            log.info("Published to {} via webhook: {}", platform, webhookUrl);
            return new PublishResult(true, "webhook-" + Instant.now().toEpochMilli(), null, Instant.now());
        } catch (Exception e) {
            log.error("Webhook publish failed for {}: {}", platform, e.getMessage());
            return new PublishResult(false, null, "Webhook failed: " + e.getMessage(), Instant.now());
        }
    }

    private PublishResult logOnly(PublishRequest request) {
        log.info("Published to {} - productId: {}, content length: {}",
            platform, request.productId(),
            request.content() != null ? request.content().length() : 0);

        return new PublishResult(true, "log-" + Instant.now().toEpochMilli(), null, Instant.now());
    }
}
