package com.atlasdeals.analytics.infrastructure.cache;

import com.atlasdeals.analytics.application.dto.DashboardMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsCacheService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsCacheService.class);
    private static final String CACHE_PREFIX = "analytics:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AnalyticsCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheDashboardMetrics(String key, DashboardMetrics metrics) {
        try {
            String json = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(CACHE_PREFIX + key, json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache dashboard metrics: {}", e.getMessage());
        }
    }

    public DashboardMetrics getDashboardMetrics(String key) {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_PREFIX + key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, DashboardMetrics.class);
        } catch (Exception e) {
            log.warn("Failed to retrieve dashboard metrics from cache: {}", e.getMessage());
            return null;
        }
    }

    public void evict(String key) {
        redisTemplate.delete(CACHE_PREFIX + key);
    }

    public void evictAll() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("Failed to evict analytics cache: {}", e.getMessage());
        }
    }
}
