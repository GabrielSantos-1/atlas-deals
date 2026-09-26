package com.atlasdeals.affiliateengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.atlasdeals.affiliateengine")
@EnableJpaRepositories(basePackages = "com.atlasdeals.affiliateengine")
public class AffiliateEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AffiliateEngineApplication.class, args);
    }
}
