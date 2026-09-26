package com.atlasdeals.imagegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan
@ConfigurationPropertiesScan
public class ImageGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageGeneratorApplication.class, args);
    }
}
