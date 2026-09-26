package com.atlasdeals.notification.application.dto;

import com.atlasdeals.notification.domain.model.Notification;
import com.atlasdeals.notification.domain.model.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationDTO(
    UUID id,
    UUID userId,
    NotificationType type,
    String title,
    String message,
    boolean read,
    Instant sentAt,
    Instant readAt
) {
    public static NotificationDTO from(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getUserId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.isRead(),
            notification.getSentAt(),
            notification.getReadAt()
        );
    }
}
