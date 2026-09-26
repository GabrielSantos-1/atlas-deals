package com.atlasdeals.promotionengine.infrastructure.web;

import com.atlasdeals.promotionengine.domain.model.Promotion;
import com.atlasdeals.promotionengine.domain.model.PromotionStatus;
import com.atlasdeals.promotionengine.domain.repository.PromotionRepository;
import com.atlasdeals.promotionengine.infrastructure.config.PromotionProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private static final Logger log = LoggerFactory.getLogger(PromotionController.class);

    private final PromotionRepository promotionRepository;
    private final PromotionProperties properties;

    public PromotionController(PromotionRepository promotionRepository,
                               PromotionProperties properties) {
        this.promotionRepository = promotionRepository;
        this.properties = properties;
    }

    @GetMapping
    public ResponseEntity<List<Promotion>> listPromotions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int pageSize = Math.min(size, properties.getMaxResultsPerPage());
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "detectedAt"));

        List<Promotion> promotions;
        if (status != null && category != null) {
            promotions = promotionRepository
                .findByCategoryAndStatus(category, PromotionStatus.valueOf(status.toUpperCase()), pageRequest);
        } else if (status != null) {
            promotions = promotionRepository
                .findByStatus(PromotionStatus.valueOf(status.toUpperCase()));
        } else if (category != null) {
            promotions = promotionRepository
                .findByCategoryAndStatus(category, null, pageRequest);
        } else {
            promotions = promotionRepository.findAll(pageRequest).getContent();
        }

        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getPromotion(@PathVariable UUID id) {
        return promotionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/top")
    public ResponseEntity<List<Promotion>> getTopPromotions(
            @RequestParam(defaultValue = "10") int limit) {
        int pageSize = Math.min(limit, properties.getMaxResultsPerPage());
        List<Promotion> top = promotionRepository
            .findTopByOrderByScoreDesc(PageRequest.of(0, pageSize));
        return ResponseEntity.ok(top);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approvePromotion(@PathVariable UUID id) {
        return promotionRepository.findById(id)
            .map(promotion -> {
                promotion.approve();
                promotionRepository.save(promotion);
                log.info("Promotion {} approved", id);
                return ResponseEntity.<Void>ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectPromotion(@PathVariable UUID id) {
        return promotionRepository.findById(id)
            .map(promotion -> {
                promotion.reject();
                promotionRepository.save(promotion);
                log.info("Promotion {} rejected", id);
                return ResponseEntity.<Void>ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", promotionRepository.countTotal());
        stats.put("detected", promotionRepository.countByStatus(PromotionStatus.DETECTED));
        stats.put("approved", promotionRepository.countByStatus(PromotionStatus.APPROVED));
        stats.put("queued", promotionRepository.countByStatus(PromotionStatus.QUEUED));
        stats.put("processing", promotionRepository.countByStatus(PromotionStatus.PROCESSING));
        stats.put("published", promotionRepository.countByStatus(PromotionStatus.PUBLISHED));
        stats.put("expired", promotionRepository.countByStatus(PromotionStatus.EXPIRED));
        stats.put("rejected", promotionRepository.countByStatus(PromotionStatus.REJECTED));
        return ResponseEntity.ok(stats);
    }
}

