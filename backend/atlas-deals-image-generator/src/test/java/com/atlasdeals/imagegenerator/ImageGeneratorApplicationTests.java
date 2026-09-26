package com.atlasdeals.imagegenerator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class ImageGeneratorApplicationTests {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
        registry.add("spring.rabbitmq.username", () -> "atlas");
        registry.add("spring.rabbitmq.password", () -> "atlas_rabbit_2024");
        registry.add("spring.rabbitmq.virtual-host", () -> "atlasdeals");
        registry.add("spring.redis.host", () -> "localhost");
        registry.add("spring.redis.port", () -> "6379");
        registry.add("spring.redis.password", () -> "atlas_redis_2024");
    }

    @Test
    void contextLoads() {
    }
}
