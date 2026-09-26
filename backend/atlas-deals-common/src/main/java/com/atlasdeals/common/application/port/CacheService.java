package com.atlasdeals.common.application.port;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {
    <T> void set(String key, T value, Duration ttl);
    <T> Optional<T> get(String key, Class<T> type);
    void delete(String key);
    boolean exists(String key);
}
