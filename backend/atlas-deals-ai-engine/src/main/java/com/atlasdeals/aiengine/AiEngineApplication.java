package com.atlasdeals.aiengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiEngineApplication.class, args);
    }
}
