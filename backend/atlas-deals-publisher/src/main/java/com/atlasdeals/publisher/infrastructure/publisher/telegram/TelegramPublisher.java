package com.atlasdeals.publisher.infrastructure.publisher.telegram;

import com.atlasdeals.publisher.application.dto.PublishRequest;
import com.atlasdeals.publisher.application.dto.PublishResult;
import com.atlasdeals.publisher.infrastructure.config.PublisherConfig;
import com.atlasdeals.publisher.infrastructure.publisher.BasePublisher;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelegramPublisher extends BasePublisher {

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";
    private static final int MAX_REQUESTS_PER_SECOND = 30;
    private static final int MAX_MESSAGE_LENGTH = 4096;

    private final RestTemplate restTemplate;
    private final String botToken;
    private final String chatId;

    public TelegramPublisher(PublisherConfig.PlatformConfig config,
                             MeterRegistry meterRegistry,
                             RestTemplateBuilder restTemplateBuilder,
                             int maxRetries,
                             long retryBackoffMs) {
        super("telegram", meterRegistry, MAX_REQUESTS_PER_SECOND, maxRetries, retryBackoffMs);
        this.botToken = config.getBotToken();
        this.chatId = config.getChatId();
        this.restTemplate = restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public boolean isAvailable() {
        return botToken != null && !botToken.isBlank()
            && chatId != null && !chatId.isBlank();
    }

    @Override
    protected PublishResult doPublish(PublishRequest request) {
        try {
            String text = request.content();
            if (text == null || text.isBlank()) {
                return new PublishResult(false, null, "Content is empty", Instant.now());
            }

            if (text.length() > MAX_MESSAGE_LENGTH) {
                text = text.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
            }

            String platformPostId;
            if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
                platformPostId = sendPhotoWithCaption(text, request.imageUrl());
            } else {
                platformPostId = sendTextMessage(text);
            }

            return new PublishResult(true, platformPostId, null, Instant.now());
        } catch (Exception e) {
            log.error("Telegram publish failed: {}", e.getMessage(), e);
            return new PublishResult(false, null, e.getMessage(), Instant.now());
        }
    }

    private String sendTextMessage(String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "HTML");
        body.put("disable_web_page_preview", false);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            TELEGRAM_API_BASE + botToken + "/sendMessage",
            buildJsonEntity(body),
            Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("result")) {
            Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
            return String.valueOf(result.get("message_id"));
        }

        throw new RuntimeException("Telegram API returned unexpected response: " + response.getBody());
    }

    private String sendPhotoWithCaption(String caption, String imageUrl) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("chat_id", chatId);
        body.put("photo", imageUrl);
        body.put("caption", caption);
        body.put("parse_mode", "HTML");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            TELEGRAM_API_BASE + botToken + "/sendPhoto",
            buildJsonEntity(body),
            Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("result")) {
            Map<String, Object> result = (Map<String, Object>) response.getBody().get("result");
            return String.valueOf(result.get("message_id"));
        }

        throw new RuntimeException("Telegram API returned unexpected response: " + response.getBody());
    }

    private HttpEntity<Map<String, Object>> buildJsonEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
