package com.atlasdeals.publisher.infrastructure.config;

import com.atlasdeals.publisher.application.port.Publisher;
import com.atlasdeals.publisher.infrastructure.publisher.GenericWebhookPublisher;
import com.atlasdeals.publisher.infrastructure.publisher.instagram.InstagramPublisher;
import com.atlasdeals.publisher.infrastructure.publisher.site.SitePublisher;
import com.atlasdeals.publisher.infrastructure.publisher.telegram.TelegramPublisher;
import com.atlasdeals.publisher.infrastructure.publisher.whatsapp.WhatsAppPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class PublisherBeanConfig {

    private static final Logger log = LoggerFactory.getLogger(PublisherBeanConfig.class);

    private final PublisherConfig publisherConfig;
    private final MeterRegistry meterRegistry;
    private final RestTemplateBuilder restTemplateBuilder;

    public PublisherBeanConfig(PublisherConfig publisherConfig,
                               MeterRegistry meterRegistry,
                               RestTemplateBuilder restTemplateBuilder) {
        this.publisherConfig = publisherConfig;
        this.meterRegistry = meterRegistry;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    public Map<String, Publisher> publisherMap() {
        Map<String, Publisher> publishers = new LinkedHashMap<>();
        int maxRetries = publisherConfig.getMaxRetries();
        long retryBackoffMs = publisherConfig.getRetryBackoffMs();

        PublisherConfig.PlatformConfig telegramConfig = publisherConfig.getPlatform("telegram");
        if (telegramConfig != null && telegramConfig.isEnabled()) {
            publishers.put("telegram", new TelegramPublisher(
                telegramConfig, meterRegistry, restTemplateBuilder, maxRetries, retryBackoffMs));
            log.info("Telegram publisher enabled");
        }

        PublisherConfig.PlatformConfig whatsappConfig = publisherConfig.getPlatform("whatsapp");
        if (whatsappConfig != null && whatsappConfig.isEnabled()) {
            publishers.put("whatsapp", new WhatsAppPublisher(
                whatsappConfig, meterRegistry, restTemplateBuilder, maxRetries, retryBackoffMs));
            log.info("WhatsApp publisher enabled");
        }

        PublisherConfig.PlatformConfig instagramConfig = publisherConfig.getPlatform("instagram");
        if (instagramConfig != null && instagramConfig.isEnabled()) {
            publishers.put("instagram", new InstagramPublisher(
                instagramConfig, meterRegistry, restTemplateBuilder, maxRetries, retryBackoffMs));
            log.info("Instagram publisher enabled");
        }

        PublisherConfig.PlatformConfig siteConfig = publisherConfig.getPlatform("site");
        if (siteConfig != null && siteConfig.isEnabled()) {
            publishers.put("site", new SitePublisher(
                siteConfig, meterRegistry, maxRetries, retryBackoffMs));
            log.info("Site publisher enabled");
        }

        String[] webhookPlatforms = {"facebook", "threads", "twitter", "discord", "newsletter", "push"};
        for (String platform : webhookPlatforms) {
            PublisherConfig.PlatformConfig config = publisherConfig.getPlatform(platform);
            if (config != null && config.isEnabled()) {
                publishers.put(platform, new GenericWebhookPublisher(
                    platform, config, meterRegistry, restTemplateBuilder, maxRetries, retryBackoffMs));
                log.info("{} publisher enabled", platform);
            }
        }

        log.info("Total publishers registered: {}", publishers.size());
        return publishers;
    }
}
