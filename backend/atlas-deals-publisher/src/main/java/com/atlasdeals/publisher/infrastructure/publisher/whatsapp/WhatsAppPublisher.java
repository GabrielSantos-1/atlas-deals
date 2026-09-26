package com.atlasdeals.publisher.infrastructure.publisher.whatsapp;

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
import java.util.List;
import java.util.Map;

public class WhatsAppPublisher extends BasePublisher {

    private static final String WHATSAPP_API_BASE = "https://graph.facebook.com/v18.0/";
    private static final int MAX_REQUESTS_PER_SECOND = 10;

    private final RestTemplate restTemplate;
    private final String phoneNumberId;
    private final String accessToken;

    public WhatsAppPublisher(PublisherConfig.PlatformConfig config,
                             MeterRegistry meterRegistry,
                             RestTemplateBuilder restTemplateBuilder,
                             int maxRetries,
                             long retryBackoffMs) {
        super("whatsapp", meterRegistry, MAX_REQUESTS_PER_SECOND, maxRetries, retryBackoffMs);
        this.phoneNumberId = config.getPhoneNumberId();
        this.accessToken = config.getAccessToken();
        this.restTemplate = restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public boolean isAvailable() {
        return phoneNumberId != null && !phoneNumberId.isBlank()
            && accessToken != null && !accessToken.isBlank();
    }

    @Override
    protected PublishResult doPublish(PublishRequest request) {
        try {
            String text = request.content();
            if (text == null || text.isBlank()) {
                return new PublishResult(false, null, "Content is empty", Instant.now());
            }

            String messageId = sendTextMessage(text);
            return new PublishResult(true, messageId, null, Instant.now());
        } catch (Exception e) {
            log.error("WhatsApp publish failed: {}", e.getMessage(), e);
            return new PublishResult(false, null, e.getMessage(), Instant.now());
        }
    }

    private String sendTextMessage(String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", phoneNumberId);
        body.put("type", "text");

        Map<String, Object> textObj = new LinkedHashMap<>();
        textObj.put("preview_url", true);
        textObj.put("body", text);
        body.put("text", textObj);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            WHATSAPP_API_BASE + phoneNumberId + "/messages",
            buildAuthEntity(body),
            Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("messages")) {
            List<Map<String, Object>> messages = (List<Map<String, Object>>) response.getBody().get("messages");
            if (messages != null && !messages.isEmpty()) {
                return String.valueOf(messages.get(0).get("id"));
            }
        }

        throw new RuntimeException("WhatsApp API returned unexpected response: " + response.getBody());
    }

    private HttpEntity<Map<String, Object>> buildAuthEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return new HttpEntity<>(body, headers);
    }
}
