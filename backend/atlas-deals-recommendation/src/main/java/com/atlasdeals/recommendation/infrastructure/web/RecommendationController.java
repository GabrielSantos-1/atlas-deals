package com.atlasdeals.recommendation.infrastructure.web;

import com.atlasdeals.recommendation.application.service.RecommendationService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/personalized")
    public ResponseEntity<List<UUID>> getPersonalized(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(userId, limit));
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<List<UUID>> getSimilar(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getSimilarProducts(productId, limit));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<UUID>> getTrending(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getTrendingProducts(category, limit));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<UUID>> getPopular(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.getPopularInCategory(category, limit));
    }

    @PostMapping("/interaction")
    public ResponseEntity<Void> recordInteraction(@RequestBody RecordInteractionRequest request) {
        recommendationService.recordInteraction(request.userId(), request.productId(), request.interactionType());
        return ResponseEntity.ok().build();
    }

    record RecordInteractionRequest(UUID userId, UUID productId, String interactionType) {}
}
