package com.atlasdeals.publisher.infrastructure.web;

import com.atlasdeals.publisher.application.service.PublicationService;
import com.atlasdeals.publisher.domain.model.Publication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/publisher")
public class PublisherController {

    private static final Logger log = LoggerFactory.getLogger(PublisherController.class);

    private final PublicationService publicationService;

    public PublisherController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publish(@Valid @RequestBody PublishRequestDTO request) {
        log.info("Received publish request for product {} on platform {}",
            request.productId(), request.platform());

        Publication publication = publicationService.schedulePublication(
            request.productId(),
            request.platform().toUpperCase(),
            request.content(),
            request.imageUrl(),
            request.affiliateUrl(),
            null
        );

        var result = publicationService.publishNow(publication.getId());

        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                "publicationId", publication.getId(),
                "status", "PUBLISHED",
                "platformPostId", result.platformPostId(),
                "publishedAt", result.publishedAt()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "publicationId", publication.getId(),
                "status", "FAILED",
                "error", result.errorMessage()
            ));
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> schedule(@Valid @RequestBody ScheduleRequestDTO request) {
        log.info("Received schedule request for product {} on platform {}, scheduled for {}",
            request.productId(), request.platform(), request.scheduledFor());

        Publication publication = publicationService.schedulePublication(
            request.productId(),
            request.platform().toUpperCase(),
            request.content(),
            request.imageUrl(),
            request.affiliateUrl(),
            request.scheduledFor()
        );

        return ResponseEntity.ok(Map.of(
            "publicationId", publication.getId(),
            "status", "SCHEDULED",
            "scheduledFor", publication.getScheduledFor()
        ));
    }

    @GetMapping("/publications/{id}")
    public ResponseEntity<?> getPublication(@PathVariable UUID id) {
        return publicationService.getPublicationStatus(id)
            .map(publication -> ResponseEntity.ok(toResponse(publication)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/publications/product/{productId}")
    public ResponseEntity<List<Map<String, Object>>> getPublicationsByProduct(@PathVariable UUID productId) {
        List<Publication> publications = publicationService.getPublicationsByProduct(productId);
        List<Map<String, Object>> response = publications.stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/publications/{id}/retry")
    public ResponseEntity<Map<String, Object>> retry(@PathVariable UUID id) {
        log.info("Retrying publication: {}", id);
        try {
            var result = publicationService.publishNow(id);
            return ResponseEntity.ok(Map.of(
                "publicationId", id,
                "status", result.success() ? "PUBLISHED" : "FAILED",
                "platformPostId", result.platformPostId() != null ? result.platformPostId() : "",
                "error", result.errorMessage() != null ? result.errorMessage() : ""
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/platforms")
    public ResponseEntity<Map<String, Object>> getPlatforms() {
        List<String> available = publicationService.getAvailablePlatforms();
        return ResponseEntity.ok(Map.of("platforms", available));
    }

    private Map<String, Object> toResponse(Publication publication) {
        return Map.of(
            "id", publication.getId(),
            "productId", publication.getProductId(),
            "platform", publication.getPlatform(),
            "platformPostId", publication.getPlatformPostId() != null ? publication.getPlatformPostId() : "",
            "status", publication.getStatus().name(),
            "content", publication.getContent() != null ? publication.getContent() : "",
            "errorMessage", publication.getErrorMessage() != null ? publication.getErrorMessage() : "",
            "scheduledFor", publication.getScheduledFor() != null ? publication.getScheduledFor().toString() : "",
            "publishedAt", publication.getPublishedAt() != null ? publication.getPublishedAt().toString() : "",
            "retryCount", publication.getRetryCount(),
            "maxRetries", publication.getMaxRetries(),
            "createdAt", publication.getCreatedAt().toString(),
            "updatedAt", publication.getUpdatedAt().toString()
        );
    }

    public record PublishRequestDTO(
        @NotNull UUID productId,
        @NotBlank String platform,
        @NotBlank String content,
        String imageUrl,
        String affiliateUrl
    ) {}

    public record ScheduleRequestDTO(
        @NotNull UUID productId,
        @NotBlank String platform,
        @NotBlank String content,
        String imageUrl,
        String affiliateUrl,
        @NotNull Instant scheduledFor
    ) {}
}
