package com.atlasdeals.promotionengine.infrastructure.scheduler;

import com.atlasdeals.promotionengine.domain.model.Promotion;
import com.atlasdeals.promotionengine.domain.model.PromotionStatus;
import com.atlasdeals.promotionengine.domain.repository.PromotionRepository;
import com.atlasdeals.promotionengine.infrastructure.config.PromotionProperties;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PromotionExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PromotionExpirationScheduler.class);

    private final PromotionRepository promotionRepository;
    private final PromotionProperties properties;

    public PromotionExpirationScheduler(PromotionRepository promotionRepository,
                                        PromotionProperties properties) {
        this.promotionRepository = promotionRepository;
        this.properties = properties;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOldPromotions() {
        log.info("Running promotion expiration check");

        int expirationDays = properties.getExpirationDays();
        Instant cutoff = Instant.now().minusSeconds(expirationDays * 86400L);

        List<Promotion> expiredPromotions = promotionRepository.findAll().stream()
            .filter(p -> p.getPublishedAt() != null
                && p.getStatus() == PromotionStatus.PUBLISHED
                && p.getPublishedAt().isBefore(cutoff))
            .toList();

        for (Promotion promotion : expiredPromotions) {
            promotion.expire();
            promotionRepository.save(promotion);
            log.info("Expired promotion {} for product {} (published: {})",
                promotion.getId(), promotion.getProductId(), promotion.getPublishedAt());
        }

        if (!expiredPromotions.isEmpty()) {
            log.info("Expired {} promotions", expiredPromotions.size());
        } else {
            log.debug("No promotions to expire");
        }
    }

    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void expireOrphanedDetectedPromotions() {
        Instant cutoff = Instant.now().minusSeconds(86400L);

        List<Promotion> stalePromotions = promotionRepository.findAll().stream()
            .filter(p -> p.getStatus() == PromotionStatus.DETECTED
                && p.getDetectedAt().isBefore(cutoff))
            .toList();

        for (Promotion promotion : stalePromotions) {
            promotion.expire();
            promotionRepository.save(promotion);
            log.info("Expired stale detected promotion {} for product {}",
                promotion.getId(), promotion.getProductId());
        }

        if (!stalePromotions.isEmpty()) {
            log.info("Expired {} stale detected promotions", stalePromotions.size());
        }
    }
}
