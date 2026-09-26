package com.atlasdeals.publisher.infrastructure.publisher.instagram;

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

public class InstagramPublisher extends BasePublisher {

    private static final String INSTAGRAM_API_BASE = "https://graph.facebook.com/v18.0/";
    private static final int MAX_REQUESTS_PER_SECOND = 5;

    private final RestTemplate restTemplate;
    private final String pageId;
    private final String accessToken;

    public InstagramPublisher(PublisherConfig.PlatformConfig config,
                              MeterRegistry meterRegistry,
                              RestTemplateBuilder restTemplateBuilder,
                              int maxRetries,
                              long retryBackoffMs) {
        super("instagram", meterRegistry, MAX_REQUESTS_PER_SECOND, maxRetries, retryBackoffMs);
        this.pageId = config.getPageId();
        this.accessToken = config.getAccessToken();
        this.restTemplate = restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();
    }

    @Override
    public boolean isAvailable() {
        return pageId != null && !pageId.isBlank()
            && accessToken != null && !accessToken.isBlank();
    }

    @Override
    protected PublishResult doPublish(PublishRequest request) {
        try {
            String text = request.content();
            if (text == null || text.isBlank()) {
                return new PublishResult(false, null, "Content is empty", Instant.now());
            }

            String igUserId = getIgUserId();
            String mediaId;

            if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
                mediaId = createMediaObject(igUserId, request.imageUrl(), text);
            } else {
                mediaId = createMediaObject(igUserId, null, text);
            }

            String containerId = publishMedia(igUserId, mediaId);
            return new PublishResult(true, containerId, null, Instant.now());
        } catch (Exception e) {
            log.error("Instagram publish failed: {}", e.getMessage(), e);
            return new PublishResult(false, null, e.getMessage(), Instant.now());
        }
    }

    private String getIgUserId() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            INSTAGRAM_API_BASE + pageId + "?fields=instagram_business_account&access_token=" + accessToken,
            Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("instagram_business_account")) {
            Map<String, Object> igAccount = (Map<String, Object>) response.getBody().get("instagram_business_account");
            return String.valueOf(igAccount.get("id"));
        }

        throw new RuntimeException("Could not retrieve Instagram Business Account ID for page: " + pageId);
    }

    private String createMediaObject(String igUserId, String imageUrl, String caption) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("image_url", imageUrl);
        body.put("caption", caption);
        body.put("access_token", accessToken);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            INSTAGRAM_API_BASE + igUserId + "/media",
            buildJsonEntity(body),
            Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("id")) {
            return String.valueOf(response.getBody().get("id"));
        }

        throw new RuntimeException("Instagram API failed to create media object: " + response.getBody());
    }

    private String publishMedia(String igUserId, String mediaId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("creation_id", mediaId);
        body.put("access_token", accessToken);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            INSTAGRAM_API_BASE + igUserId + "/media_publish",
            buildJsonEntity(body),
            Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("id")) {
            return String.valueOf(response.getBody().get("id"));
        }

        throw new RuntimeException("Instagram API failed to publish media: " + response.getBody());
    }

    private HttpEntity<Map<String, Object>> buildJsonEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
