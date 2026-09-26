package com.atlasdeals.gateway.infrastructure.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("collector", r -> r.path("/api/collector/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("collectorCircuitBreaker")
                                .setFallbackUri("forward:/fallback/collector")))
                        .uri("lb://atlas-deals-collector"))
                .route("price-history", r -> r.path("/api/price-history/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("priceHistoryCircuitBreaker")
                                .setFallbackUri("forward:/fallback/price-history")))
                        .uri("lb://atlas-deals-price-history"))
                .route("promotions", r -> r.path("/api/promotions/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("promotionsCircuitBreaker")
                                .setFallbackUri("forward:/fallback/promotions")))
                        .uri("lb://atlas-deals-promotion-engine"))
                .route("affiliate", r -> r.path("/api/affiliate/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("affiliateCircuitBreaker")
                                .setFallbackUri("forward:/fallback/affiliate")))
                        .uri("lb://atlas-deals-affiliate-engine"))
                .route("ai", r -> r.path("/api/ai/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("aiCircuitBreaker")
                                .setFallbackUri("forward:/fallback/ai")))
                        .uri("lb://atlas-deals-ai-engine"))
                .route("publisher", r -> r.path("/api/publisher/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("publisherCircuitBreaker")
                                .setFallbackUri("forward:/fallback/publisher")))
                        .uri("lb://atlas-deals-publisher"))
                .route("analytics", r -> r.path("/api/analytics/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("analyticsCircuitBreaker")
                                .setFallbackUri("forward:/fallback/analytics")))
                        .uri("lb://atlas-deals-analytics"))
                .route("auth", r -> r.path("/api/auth/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("authCircuitBreaker")
                                .setFallbackUri("forward:/fallback/auth")))
                        .uri("lb://atlas-deals-auth"))
                .route("users", r -> r.path("/api/users/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("usersCircuitBreaker")
                                .setFallbackUri("forward:/fallback/users")))
                        .uri("lb://atlas-deals-user"))
                .route("search", r -> r.path("/api/search/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("searchCircuitBreaker")
                                .setFallbackUri("forward:/fallback/search")))
                        .uri("lb://atlas-deals-search"))
                .route("recommendations", r -> r.path("/api/recommendations/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("recommendationsCircuitBreaker")
                                .setFallbackUri("forward:/fallback/recommendations")))
                        .uri("lb://atlas-deals-recommendation"))
                .route("images", r -> r.path("/api/images/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("imagesCircuitBreaker")
                                .setFallbackUri("forward:/fallback/images")))
                        .uri("lb://atlas-deals-image-generator"))
                .route("notifications", r -> r.path("/api/notifications/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("notificationsCircuitBreaker")
                                .setFallbackUri("forward:/fallback/notifications")))
                        .uri("lb://atlas-deals-notification"))
                .route("scheduler", r -> r.path("/api/scheduler/**")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("schedulerCircuitBreaker")
                                .setFallbackUri("forward:/fallback/scheduler")))
                        .uri("lb://atlas-deals-scheduler"))
                .build();
    }
}
