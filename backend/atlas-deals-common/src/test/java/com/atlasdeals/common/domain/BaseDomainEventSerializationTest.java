package com.atlasdeals.common.domain;

import com.atlasdeals.common.domain.events.ContentGeneratedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BaseDomainEventSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void eventIdAndOccurredOnSurviveSerializationRoundTrip() throws Exception {
        ContentGeneratedEvent original = new ContentGeneratedEvent(
            ProductId.generate(),
            Map.of("site", "Published content"),
            "Title",
            "Description",
            "Summary",
            "SEO description",
            "#deal",
            "Electronics",
            "deal,offer",
            "HIGH",
            "Technical summary",
            "Pros",
            "Cons",
            "Deal hunter",
            "https://images.example/deal.png",
            "Example Store",
            new BigDecimal("199.90"),
            new BigDecimal("299.90")
        );

        String json = objectMapper.writeValueAsString(original);
        ContentGeneratedEvent restored = objectMapper.readValue(json, ContentGeneratedEvent.class);

        assertThat(restored.getEventId())
            .as("eventId must survive the round trip so it can be used as an idempotency key")
            .isEqualTo(original.getEventId());
        assertThat(restored.getOccurredOn()).isEqualTo(original.getOccurredOn());
        assertThat(restored.getEventType()).isEqualTo(original.getEventType());
        assertThat(restored.getProductId()).isEqualTo(original.getProductId());
        assertThat(restored.getContentByPlatform()).isEqualTo(original.getContentByPlatform());
    }
}
