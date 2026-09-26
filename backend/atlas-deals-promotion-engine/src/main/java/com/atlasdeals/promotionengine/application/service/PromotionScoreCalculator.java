package com.atlasdeals.promotionengine.application.service;

import com.atlasdeals.promotionengine.domain.model.ScoringRule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PromotionScoreCalculator {

    private static final Logger log = LoggerFactory.getLogger(PromotionScoreCalculator.class);

    public ScoreResult calculate(PromotionData data, Map<ScoringRule, Double> ruleWeights) {
        Map<String, Double> ruleScores = new LinkedHashMap<>();
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        for (Map.Entry<ScoringRule, Double> entry : ruleWeights.entrySet()) {
            ScoringRule rule = entry.getKey();
            double weight = entry.getValue();
            if (weight <= 0) continue;

            double ruleScore = scoreRule(rule, data);
            ruleScores.put(rule.name(), ruleScore);
            totalWeightedScore += ruleScore * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0.0) {
            return new ScoreResult(BigDecimal.ZERO, ruleScores);
        }

        BigDecimal finalScore = BigDecimal.valueOf(totalWeightedScore / totalWeight * 100.0)
            .setScale(2, RoundingMode.HALF_EVEN);

        log.debug("Promotion score calculated: {} from {} rules", finalScore, ruleScores.size());
        return new ScoreResult(finalScore, ruleScores);
    }

    private double scoreRule(ScoringRule rule, PromotionData data) {
        return switch (rule) {
            case DISCOUNT_DEPTH -> scoreDiscountDepth(data);
            case LOWEST_PRICE_30_DAYS -> scoreLowestPrice30Days(data);
            case LOWEST_PRICE_HISTORICAL -> scoreLowestPriceHistorical(data);
            case COUPON_AVAILABLE -> scoreCouponAvailable(data);
            case HIGH_CONVERSION -> scoreHighConversion(data);
            case STORE_REPUTATION -> scoreStoreReputation(data);
            case STOCK_AVAILABLE -> scoreStockAvailable(data);
            case POPULAR_PRODUCT -> scorePopularProduct(data);
            case SHIPPING_FREE -> scoreShippingFree(data);
        };
    }

    private double scoreDiscountDepth(PromotionData data) {
        double discountPct = data.discountPercentage();
        if (discountPct <= 0) return 0.0;
        return Math.min(discountPct / 100.0, 0.5);
    }

    private double scoreLowestPrice30Days(PromotionData data) {
        if (data.lowestPrice30Days() == null) return 0.0;
        if (data.currentPrice().compareTo(data.lowestPrice30Days()) >= 0) return 0.0;
        double ratio = data.currentPrice().doubleValue() / data.lowestPrice30Days().doubleValue();
        return Math.min(1.0, Math.max(0.0, 1.0 - ratio));
    }

    private double scoreLowestPriceHistorical(PromotionData data) {
        if (data.lowestPriceHistorical() == null) return 0.0;
        if (data.currentPrice().compareTo(data.lowestPriceHistorical()) >= 0) return 0.0;
        double ratio = data.currentPrice().doubleValue() / data.lowestPriceHistorical().doubleValue();
        return Math.min(1.0, Math.max(0.0, 1.0 - ratio));
    }

    private double scoreCouponAvailable(PromotionData data) {
        return data.hasCoupon() ? 1.0 : 0.0;
    }

    private double scoreHighConversion(PromotionData data) {
        double rate = data.conversionRate();
        if (rate >= 0.10) return 1.0;
        if (rate >= 0.05) return 0.7;
        if (rate >= 0.02) return 0.4;
        if (rate > 0.0) return 0.2;
        return 0.0;
    }

    private double scoreStoreReputation(PromotionData data) {
        return switch (data.storeName() != null ? data.storeName().toLowerCase() : "") {
            case "amazon" -> 1.0;
            case "magalu", "magazine luiza" -> 0.9;
            case "mercado livre" -> 0.85;
            case "americanas" -> 0.8;
            case "shopee" -> 0.75;
            case "casas bahia" -> 0.7;
            case "kabum" -> 0.85;
            case "ponto frio" -> 0.7;
            case "extra" -> 0.65;
            case "carrefour" -> 0.6;
            default -> 0.5;
        };
    }

    private double scoreStockAvailable(PromotionData data) {
        return data.inStock() ? 1.0 : 0.0;
    }

    private double scorePopularProduct(PromotionData data) {
        if (data.totalSales() == null || data.totalSales() <= 0) return 0.0;
        if (data.totalSales() >= 1000) return 1.0;
        if (data.totalSales() >= 500) return 0.8;
        if (data.totalSales() >= 100) return 0.5;
        if (data.totalSales() >= 50) return 0.3;
        return 0.1;
    }

    private double scoreShippingFree(PromotionData data) {
        return data.isShippingFree() ? 1.0 : 0.0;
    }

    public record PromotionData(
        BigDecimal currentPrice,
        BigDecimal originalPrice,
        double discountPercentage,
        BigDecimal lowestPrice30Days,
        BigDecimal lowestPriceHistorical,
        boolean hasCoupon,
        double conversionRate,
        String storeName,
        boolean inStock,
        Integer totalSales,
        boolean isShippingFree
    ) {}

    public record ScoreResult(
        BigDecimal totalScore,
        Map<String, Double> ruleScores
    ) {}
}
