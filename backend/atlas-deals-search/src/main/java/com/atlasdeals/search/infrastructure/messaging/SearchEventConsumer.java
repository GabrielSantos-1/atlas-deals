package com.atlasdeals.search.infrastructure.messaging;

import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.events.ProductCollectedEvent;
import com.atlasdeals.search.application.service.IndexingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SearchEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SearchEventConsumer.class);

    private final IndexingService indexingService;
    private final ObjectMapper objectMapper;

    public SearchEventConsumer(IndexingService indexingService, ObjectMapper objectMapper) {
        this.indexingService = indexingService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "search.index.q")
    public void handleProductCollected(@Payload String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            ProductId productId = ProductId.of(UUID.fromString(json.get("productId").asText()));

            indexingService.indexProduct(
                productId,
                json.get("title").asText(),
                json.has("description") ? json.get("description").asText() : null,
                new BigDecimal(json.get("price").asText()),
                json.has("originalPrice") ? new BigDecimal(json.get("originalPrice").asText()) : BigDecimal.ZERO,
                json.has("category") ? json.get("category").asText() : null,
                json.has("brand") ? json.get("brand").asText() : null,
                json.get("storeName").asText(),
                json.get("url").asText(),
                json.has("imageUrl") ? json.get("imageUrl").asText() : null,
                json.has("inStock") && json.get("inStock").asBoolean(),
                json.has("currency") ? json.get("currency").asText() : "BRL"
            );
            log.info("Indexed product from event: {}", productId.getValue());
        } catch (Exception e) {
            log.error("Failed to process product collected event: {}", e.getMessage(), e);
        }
    }
}
