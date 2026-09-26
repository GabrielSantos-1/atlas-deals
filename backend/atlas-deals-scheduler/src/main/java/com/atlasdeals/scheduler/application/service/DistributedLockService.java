package com.atlasdeals.scheduler.application.service;

import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockService {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String LOCK_PREFIX = "scheduler:lock:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean acquireLock(String jobName) {
        String lockKey = LOCK_PREFIX + jobName;
        String lockValue = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, DEFAULT_TTL);
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired lock for job: {}", jobName);
            return true;
        }
        log.debug("Failed to acquire lock for job: {} (already running)", jobName);
        return false;
    }

    public void releaseLock(String jobName) {
        String lockKey = LOCK_PREFIX + jobName;
        redisTemplate.delete(lockKey);
        log.debug("Released lock for job: {}", jobName);
    }
}
