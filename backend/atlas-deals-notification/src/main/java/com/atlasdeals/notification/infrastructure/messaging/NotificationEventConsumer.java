package com.atlasdeals.notification.infrastructure.messaging;

import com.atlasdeals.notification.application.service.NotificationService;
import com.atlasdeals.notification.domain.model.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "notification.q")
    public void handleNotificationEvent(@Payload String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            UUID userId = UUID.fromString(json.get("userId").asText());
            String typeStr = json.get("type").asText();
            String title = json.get("title").asText();
            String body = json.has("message") ? json.get("message").asText() : title;

            NotificationType type = NotificationType.valueOf(typeStr);
            notificationService.send(userId, type, title, body);
            log.info("Processed notification event for user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Failed to process notification event: {}", e.getMessage(), e);
        }
    }
}
