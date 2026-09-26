package com.atlasdeals.analytics.infrastructure.messaging;

import com.atlasdeals.analytics.application.service.AnalyticsService;
import com.atlasdeals.analytics.domain.model.AnalyticsEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventConsumer.class);

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public AnalyticsEventConsumer(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "analytics.click.q")
    public void handleClickEvent(Map<String, Object> message) {
        try {
            AnalyticsEvent event = mapToEvent(message, "CLICK");
            analyticsService.recordEvent(event);
            log.debug("Processed click event for product {}", event.getProductId());
        } catch (Exception e) {
            log.error("Failed to process click event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "analytics.conversion.q")
    public void handleConversionEvent(Map<String, Object> message) {
        try {
            AnalyticsEvent event = mapToEvent(message, "CONVERSION");
            analyticsService.recordEvent(event);
            log.debug("Processed conversion event for product {}", event.getProductId());
        } catch (Exception e) {
            log.error("Failed to process conversion event: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private AnalyticsEvent mapToEvent(Map<String, Object> message, String defaultEventType) {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setClickId(getUUID(message.get("clickId")));
        event.setProductId(getUUID(message.get("productId")));
        event.setProductTitle(getString(message.get("productTitle")));
        event.setStoreName(getString(message.get("storeName")));
        event.setCategory(getString(message.get("category")));
        event.setPlatform(getString(message.get("platform")));
        event.setSource(getString(message.get("source")));
        event.setCampaignId(getString(message.get("campaignId")));
        event.setDeviceType(getString(message.get("deviceType")));
        event.setEventType(getString(message.get("eventType"), defaultEventType));
        event.setRevenue(getBigDecimal(message.get("revenue")));
        event.setCurrency(getString(message.get("currency")));
        event.setConverted(getBoolean(message.get("converted")));
        event.setEventTime(getInstant(message.get("eventTime")));
        event.setProcessedAt(Instant.now());

        if (message.get("metadata") != null) {
            try {
                event.setMetadata(objectMapper.writeValueAsString(message.get("metadata")));
            } catch (Exception e) {
                log.warn("Failed to serialize metadata: {}", e.getMessage());
            }
        }

        return event;
    }

    private UUID getUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof String s) return UUID.fromString(s);
        return null;
    }

    private String getString(Object value) {
        return value != null ? value.toString() : null;
    }

    private String getString(Object value, String defaultValue) {
        return value != null ? value.toString() : defaultValue;
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (value instanceof String s) return new BigDecimal(s);
        return null;
    }

    private boolean getBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return false;
    }

    private Instant getInstant(Object value) {
        if (value == null) return Instant.now();
        if (value instanceof Instant i) return i;
        if (value instanceof String s) return Instant.parse(s);
        if (value instanceof Long l) return Instant.ofEpochMilli(l);
        return Instant.now();
    }
}
