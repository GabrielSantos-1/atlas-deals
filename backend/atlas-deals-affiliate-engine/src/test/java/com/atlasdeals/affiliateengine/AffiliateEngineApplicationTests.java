package com.atlasdeals.affiliateengine;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.rabbitmq.host=localhost",
    "spring.rabbitmq.port=5672",
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
class AffiliateEngineApplicationTests {

    @Test
    void contextLoads() {
    }
}
