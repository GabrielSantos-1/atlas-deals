package com.atlasdeals.gateway.infrastructure.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final long DEFAULT_CAPACITY = 100;
    private static final double DEFAULT_REFILL_RATE = 20.0;
    private static final int DEFAULT_TTL = 60;
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RedisScript<List> rateLimitScript;

    public RateLimitingFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText("""
                local key = KEYS[1]
                local now = tonumber(ARGV[1])
                local capacity = tonumber(ARGV[2])
                local refillRate = tonumber(ARGV[3])
                local ttl = tonumber(ARGV[4])

                local data = redis.call('hmget', key, 'tokens', 'last_refill')
                local tokens = tonumber(data[1]) or capacity
                local lastRefill = tonumber(data[2]) or now

                local elapsed = math.max(0, now - lastRefill) / 1000
                tokens = math.min(capacity, tokens + elapsed * refillRate)
                lastRefill = now

                if tokens >= 1 then
                    tokens = tokens - 1
                    redis.call('hmset', key, 'tokens', tokens, 'last_refill', lastRefill)
                    redis.call('expire', key, ttl)
                    return {1, tokens}
                else
                    redis.call('expire', key, ttl)
                    return {0, tokens}
                end
                """);
        script.setResultType(List.class);
        this.rateLimitScript = script;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress == null || remoteAddress.getAddress() == null) {
            return chain.filter(exchange);
        }

        String clientIp = remoteAddress.getAddress().getHostAddress();
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + clientIp;
        long now = Instant.now().toEpochMilli();

        return redisTemplate.execute(
                rateLimitScript,
                List.of(rateLimitKey),
                List.of(String.valueOf(now), String.valueOf(DEFAULT_CAPACITY),
                        String.valueOf(DEFAULT_REFILL_RATE), String.valueOf(DEFAULT_TTL))
        ).flatMap(result -> {
            if (result instanceof List<?> results && results.size() >= 2) {
                long allowed = ((Number) results.get(0)).longValue();
                long remaining = ((Number) results.get(1)).longValue();
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remaining));
                if (allowed == 1) {
                    return chain.filter(exchange);
                }
            }
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
