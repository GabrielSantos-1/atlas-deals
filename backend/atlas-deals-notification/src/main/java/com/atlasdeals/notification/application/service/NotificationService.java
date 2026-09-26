package com.atlasdeals.notification.application.service;

import com.atlasdeals.notification.application.dto.NotificationDTO;
import com.atlasdeals.notification.domain.model.Notification;
import com.atlasdeals.notification.domain.model.NotificationType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EntityManager entityManager;

    public NotificationService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public NotificationDTO send(UUID userId, NotificationType type, String title, String message) {
        Notification notification = new Notification(userId, type, title, message);
        entityManager.persist(notification);
        log.info("Notification sent to user {}: {}", userId, title);
        return NotificationDTO.from(notification);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = entityManager.find(Notification.class, notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.markAsRead();
            entityManager.merge(notification);
            log.info("Notification {} marked as read for user {}", notificationId, userId);
        }
    }

    public List<NotificationDTO> getUnread(UUID userId) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.userId = :userId AND n.read = false ORDER BY n.sentAt DESC",
            Notification.class);
        query.setParameter("userId", userId);
        return query.getResultList().stream().map(NotificationDTO::from).toList();
    }

    public List<NotificationDTO> getHistory(UUID userId, int page, int size) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.sentAt DESC",
            Notification.class);
        query.setParameter("userId", userId);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList().stream().map(NotificationDTO::from).toList();
    }

    @Transactional
    public void sendPush(UUID userId, String title, String message) {
        Notification notification = new Notification(userId, NotificationType.PROMOTION_ALERT, title, message);
        entityManager.persist(notification);
        log.info("Push notification sent to user {}: {}", userId, title);
    }

    @Transactional
    public void sendEmail(UUID userId, String title, String message) {
        Notification notification = new Notification(userId, NotificationType.WEEKLY_DIGEST, title, message);
        entityManager.persist(notification);
        log.info("Email notification sent to user {}: {}", userId, title);
    }

    public List<NotificationDTO> getAll(int page, int size) {
        TypedQuery<Notification> query = entityManager.createQuery(
            "SELECT n FROM Notification n ORDER BY n.sentAt DESC", Notification.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList().stream().map(NotificationDTO::from).toList();
    }
}
