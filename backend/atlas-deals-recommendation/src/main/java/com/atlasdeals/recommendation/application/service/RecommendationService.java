package com.atlasdeals.recommendation.application.service;

import com.atlasdeals.recommendation.domain.model.UserInteraction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private static final String TRENDING_KEY = "recommendations:trending";
    private static final String USER_MATRIX_PREFIX = "recommendations:user:";

    private final EntityManager entityManager;
    private final StringRedisTemplate redisTemplate;

    public RecommendationService(EntityManager entityManager, StringRedisTemplate redisTemplate) {
        this.entityManager = entityManager;
        this.redisTemplate = redisTemplate;
    }

    public List<UUID> getPersonalizedRecommendations(UUID userId, int limit) {
        Set<String> interactedProducts = redisTemplate.opsForZSet().reverseRange(USER_MATRIX_PREFIX + userId, 0, -1);
        if (interactedProducts == null || interactedProducts.isEmpty()) {
            return getTrendingProducts(null, limit);
        }

        List<Object[]> results = entityManager.createQuery(
            "SELECT ui.productId, SUM(ui.score) as totalScore " +
            "FROM UserInteraction ui " +
            "WHERE ui.userId IN (SELECT DISTINCT u2.userId FROM UserInteraction u2 WHERE u2.productId IN :products AND u2.userId != :userId) " +
            "AND ui.userId != :userId " +
            "GROUP BY ui.productId " +
            "ORDER BY totalScore DESC", Object[].class)
            .setParameter("products", interactedProducts.stream().map(UUID::fromString).toList())
            .setParameter("userId", userId)
            .setMaxResults(limit)
            .getResultList();

        return results.stream()
            .map(r -> (UUID) r[0])
            .collect(Collectors.toList());
    }

    public List<UUID> getSimilarProducts(UUID productId, int limit) {
        TypedQuery<UUID> query = entityManager.createQuery(
            "SELECT ui2.productId FROM UserInteraction ui1 " +
            "JOIN UserInteraction ui2 ON ui1.userId = ui2.userId AND ui2.productId != :productId " +
            "WHERE ui1.productId = :productId " +
            "GROUP BY ui2.productId " +
            "ORDER BY COUNT(DISTINCT ui1.userId) DESC", UUID.class);
        query.setParameter("productId", productId);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<UUID> getTrendingProducts(String category, int limit) {
        String key = category != null ? TRENDING_KEY + ":" + category : TRENDING_KEY;
        Set<String> trending = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        if (trending == null || trending.isEmpty()) {
            return getPopularInCategory(category, limit);
        }
        return trending.stream().map(UUID::fromString).toList();
    }

    public List<UUID> getPopularInCategory(String category, int limit) {
        String jpql = "SELECT ui.productId FROM UserInteraction ui " +
                      (category != null ? "WHERE ui.productId IN (SELECT p.id FROM Product p WHERE p.category = :category) " : "") +
                      "GROUP BY ui.productId ORDER BY COUNT(ui.id) DESC";

        TypedQuery<UUID> query = entityManager.createQuery(jpql, UUID.class);
        if (category != null) {
            query.setParameter("category", category);
        }
        query.setMaxResults(limit);
        return query.getResultList();
    }

    public List<UUID> getRecommendedForUser(UUID userId, String category, int limit) {
        if (category != null && !category.isBlank()) {
            String key = TRENDING_KEY + ":" + category;
            Set<String> trending = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
            if (trending != null && !trending.isEmpty()) {
                return trending.stream().map(UUID::fromString).toList();
            }
            return getPopularInCategory(category, limit);
        }
        return getPersonalizedRecommendations(userId, limit);
    }

    @Transactional
    public void recordInteraction(UUID userId, UUID productId, String interactionType) {
        UserInteraction.InteractionType type;
        try {
            type = UserInteraction.InteractionType.valueOf(interactionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown interaction type: {}", interactionType);
            return;
        }

        UserInteraction interaction = new UserInteraction(userId, productId, type.name(), type.getDefaultScore());
        entityManager.persist(interaction);

        updateTrendingCache(productId, type, null);
        updateUserMatrix(userId, productId, type.getDefaultScore());
        log.info("Recorded interaction: user={}, product={}, type={}", userId, productId, type);
    }

    private void updateTrendingCache(UUID productId, UserInteraction.InteractionType type, String category) {
        double increment = type.getDefaultScore();
        redisTemplate.opsForZSet().incrementScore(TRENDING_KEY, productId.toString(), increment);
        if (category != null) {
            redisTemplate.opsForZSet().incrementScore(TRENDING_KEY + ":" + category, productId.toString(), increment);
        }
        redisTemplate.expire(TRENDING_KEY, Duration.ofHours(24));
    }

    private void updateUserMatrix(UUID userId, UUID productId, double score) {
        String key = USER_MATRIX_PREFIX + userId;
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), score);
        redisTemplate.expire(key, Duration.ofDays(30));
    }
}
