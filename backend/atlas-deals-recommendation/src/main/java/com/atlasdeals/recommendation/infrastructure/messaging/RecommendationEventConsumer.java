package com.atlasdeals.recommendation.infrastructure.messaging;

import com.atlasdeals.recommendation.application.service.RecommendationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class RecommendationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEventConsumer.class);

    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public RecommendationEventConsumer(RecommendationService recommendationService, ObjectMapper objectMapper) {
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "recommendation.q")
    public void handleAnalyticsEvent(@Payload String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            UUID userId = UUID.fromString(json.get("userId").asText());
            UUID productId = UUID.fromString(json.get("productId").asText());
            String eventType = json.get("eventType").asText();

            String interactionType;
            if (eventType.contains("click")) {
                interactionType = "CLICK";
            } else if (eventType.contains("conversion")) {
                interactionType = "CONVERSION";
            } else if (eventType.contains("favorite")) {
                interactionType = "FAVORITE";
            } else if (eventType.contains("share")) {
                interactionType = "SHARE";
            } else {
                interactionType = "VIEW";
            }

            recommendationService.recordInteraction(userId, productId, interactionType);
            log.info("Processed analytics event: user={}, product={}, type={}", userId, productId, interactionType);
        } catch (Exception e) {
            log.error("Failed to process analytics event: {}", e.getMessage(), e);
        }
    }
}
