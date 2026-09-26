package com.atlasdeals.promotionengine.application.service;

import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.events.ProductCollectedEvent;
import com.atlasdeals.common.domain.events.PromotionDetectedEvent;
import com.atlasdeals.promotionengine.application.service.PromotionScoreCalculator.PromotionData;
import com.atlasdeals.promotionengine.application.service.PromotionScoreCalculator.ScoreResult;
import com.atlasdeals.promotionengine.domain.model.Promotion;
import com.atlasdeals.promotionengine.domain.model.PromotionStatus;
import com.atlasdeals.promotionengine.domain.model.ScoringRule;
import com.atlasdeals.promotionengine.domain.repository.PromotionRepository;
import com.atlasdeals.promotionengine.infrastructure.config.PromotionProperties;
import com.atlasdeals.promotionengine.infrastructure.messaging.PromotionEventPublisher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromotionDetectionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionDetectionService.class);

    private final PromotionRepository promotionRepository;
    private final PromotionScoreCalculator scoreCalculator;
    private final PromotionEventPublisher eventPublisher;
    private final PromotionProperties properties;

    public PromotionDetectionService(PromotionRepository promotionRepository,
                                     PromotionScoreCalculator scoreCalculator,
                                     PromotionEventPublisher eventPublisher,
                                     PromotionProperties properties) {
        this.promotionRepository = promotionRepository;
        this.scoreCalculator = scoreCalculator;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @Transactional
    public void analyzeProduct(ProductCollectedEvent event) {
        log.info("Analyzing product {} for promotion potential", event.getProductId().getValue());

        String url = event.getUrl();
        if (url == null || url.isBlank()) {
            log.debug("Product {} skipped: no URL", event.getProductId().getValue());
            return;
        }

        if (!event.isInStock()) {
            log.debug("Product {} skipped: out of stock", event.getProductId().getValue());
            return;
        }

        BigDecimal discountPct = calculateDiscountPercentage(event);
        if (discountPct.compareTo(BigDecimal.valueOf(properties.getMinDiscountPercent())) < 0) {
            log.debug("Product {} skipped: discount {}% below threshold {}%",
                event.getProductId().getValue(), discountPct, properties.getMinDiscountPercent());
            return;
        }

        if (isDuplicate(event.getProductId().getValue())) {
            log.info("Product {} skipped: already detected/published in last 7 days",
                event.getProductId().getValue());
            return;
        }

        BigDecimal currentPrice = event.getPrice().getAmount();
        BigDecimal originalPrice = event.getOriginalPrice() != null
            ? event.getOriginalPrice().getAmount() : currentPrice;

        PromotionData data = new PromotionData(
            currentPrice,
            originalPrice,
            discountPct.doubleValue(),
            null,
            null,
            false,
            0.0,
            event.getStoreName(),
            event.isInStock(),
            0,
            false
        );

        Map<ScoringRule, Double> ruleWeights = buildRuleWeights();
        ScoreResult result = scoreCalculator.calculate(data, ruleWeights);

        BigDecimal score = result.totalScore();
        List<String> rulesApplied = new ArrayList<>(result.ruleScores().keySet());

        log.info("Product {} scored: {} (threshold: {})",
            event.getProductId().getValue(), score, properties.getScoreThreshold());

        if (score.compareTo(BigDecimal.valueOf(properties.getScoreThreshold())) < 0) {
            log.debug("Product {} did not reach score threshold", event.getProductId().getValue());
            return;
        }

        String reason = buildReason(discountPct, score, rulesApplied);

        List<String> tags = buildTags(event);

        Promotion promotion = Promotion.builder()
            .productId(event.getProductId().getValue())
            .title(event.getTitle())
            .currentPrice(currentPrice)
            .originalPrice(originalPrice)
            .discountPercentage(discountPct)
            .score(score)
            .category(event.getCategory())
            .storeName(event.getStoreName())
            .storeId(event.getStoreId().getValue())
            .url(url)
            .imageUrl(event.getImageUrl())
            .inStock(event.isInStock())
            .status(PromotionStatus.DETECTED)
            .tags(tags)
            .reason(reason)
            .detectedAt(Instant.now())
            .revenue(BigDecimal.ZERO)
            .build();

        promotionRepository.save(promotion);
        log.info("Promotion saved with id {} for product {}", promotion.getId(),
            event.getProductId().getValue());

        Money currentPriceMoney = Money.of(currentPrice, event.getPrice().getCurrency());
        Money originalPriceMoney = event.getOriginalPrice() != null
            ? Money.of(originalPrice, event.getOriginalPrice().getCurrency())
            : currentPriceMoney;

        PromotionDetectedEvent detectedEvent = new PromotionDetectedEvent(
            event.getProductId(),
            event.getTitle(),
            currentPriceMoney,
            originalPriceMoney,
            discountPct,
            score,
            event.getCategory(),
            event.getStoreName(),
            url,
            event.getImageUrl(),
            event.isInStock(),
            tags,
            reason
        );

        eventPublisher.publishPromotionDetected(detectedEvent);
        log.info("PromotionDetectedEvent published for product {}", event.getProductId().getValue());
    }

    public boolean isDuplicate(java.util.UUID productId) {
        Optional<Promotion> existing = promotionRepository.findByProductId(productId);
        if (existing.isEmpty()) return false;

        Promotion prom = existing.get();
        if (prom.getStatus() == PromotionStatus.PUBLISHED || prom.getStatus() == PromotionStatus.DETECTED
            || prom.getStatus() == PromotionStatus.APPROVED || prom.getStatus() == PromotionStatus.QUEUED
            || prom.getStatus() == PromotionStatus.PROCESSING) {
            Instant publishedAt = prom.getPublishedAt() != null ? prom.getPublishedAt() : prom.getDetectedAt();
            if (publishedAt.isAfter(Instant.now().minus(7, ChronoUnit.DAYS))) {
                return true;
            }
        }
        return false;
    }

    private BigDecimal calculateDiscountPercentage(ProductCollectedEvent event) {
        if (event.getOriginalPrice() == null
            || event.getOriginalPrice().getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return event.getDiscountPercentage();
    }

    private String buildReason(BigDecimal discountPct, BigDecimal score, List<String> rules) {
        return String.format("Discount: %.2f%%, Score: %.2f, Rules applied: %s",
            discountPct, score, String.join(", ", rules));
    }

    private List<String> buildTags(ProductCollectedEvent event) {
        List<String> tags = new ArrayList<>();
        tags.add("promotion");
        if (event.getCategory() != null) {
            tags.add(event.getCategory().toLowerCase().replaceAll("\\s+", "-"));
        }
        if (event.getBrand() != null) {
            tags.add("brand:" + event.getBrand().toLowerCase().replaceAll("\\s+", "-"));
        }
        return tags;
    }

    private Map<ScoringRule, Double> buildRuleWeights() {
        Map<ScoringRule, Double> weights = new HashMap<>();
        PromotionProperties.RuleConfig discountDepth = properties.getRules().get("discount-depth");
        if (discountDepth != null && discountDepth.isEnabled()) {
            weights.put(ScoringRule.DISCOUNT_DEPTH, discountDepth.getWeight());
        }
        PromotionProperties.RuleConfig lowestPrice30d = properties.getRules().get("lowest-price-30d");
        if (lowestPrice30d != null && lowestPrice30d.isEnabled()) {
            weights.put(ScoringRule.LOWEST_PRICE_30_DAYS, lowestPrice30d.getWeight());
        }
        PromotionProperties.RuleConfig lowestPriceHistorical = properties.getRules().get("lowest-price-historical");
        if (lowestPriceHistorical != null && lowestPriceHistorical.isEnabled()) {
            weights.put(ScoringRule.LOWEST_PRICE_HISTORICAL, lowestPriceHistorical.getWeight());
        }
        PromotionProperties.RuleConfig couponAvailable = properties.getRules().get("coupon-available");
        if (couponAvailable != null && couponAvailable.isEnabled()) {
            weights.put(ScoringRule.COUPON_AVAILABLE, couponAvailable.getWeight());
        }
        PromotionProperties.RuleConfig highConversion = properties.getRules().get("high-conversion");
        if (highConversion != null && highConversion.isEnabled()) {
            weights.put(ScoringRule.HIGH_CONVERSION, highConversion.getWeight());
        }
        PromotionProperties.RuleConfig storeReputation = properties.getRules().get("store-reputation");
        if (storeReputation != null && storeReputation.isEnabled()) {
            weights.put(ScoringRule.STORE_REPUTATION, storeReputation.getWeight());
        }
        PromotionProperties.RuleConfig stockAvailable = properties.getRules().get("stock-available");
        if (stockAvailable != null && stockAvailable.isEnabled()) {
            weights.put(ScoringRule.STOCK_AVAILABLE, stockAvailable.getWeight());
        }
        PromotionProperties.RuleConfig popularProduct = properties.getRules().get("popular-product");
        if (popularProduct != null && popularProduct.isEnabled()) {
            weights.put(ScoringRule.POPULAR_PRODUCT, popularProduct.getWeight());
        }
        PromotionProperties.RuleConfig shippingFree = properties.getRules().get("shipping-free");
        if (shippingFree != null && shippingFree.isEnabled()) {
            weights.put(ScoringRule.SHIPPING_FREE, shippingFree.getWeight());
        }
        return weights;
    }
}
