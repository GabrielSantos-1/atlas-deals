package com.atlasdeals.notification.infrastructure.web;

import com.atlasdeals.notification.application.dto.NotificationDTO;
import com.atlasdeals.notification.application.service.NotificationService;
import com.atlasdeals.notification.domain.model.NotificationType;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getAll(page, size));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnread(@RequestParam UUID userId) {
        return ResponseEntity.ok(notificationService.getUnread(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, @RequestParam UUID userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationDTO> send(@RequestBody SendNotificationRequest request) {
        NotificationDTO notification = notificationService.send(
            request.userId(), request.type(), request.title(), request.message());
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    record SendNotificationRequest(UUID userId, NotificationType type, String title, String message) {}
}
