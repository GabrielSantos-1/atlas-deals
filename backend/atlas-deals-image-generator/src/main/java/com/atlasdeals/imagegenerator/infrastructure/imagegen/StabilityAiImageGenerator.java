package com.atlasdeals.imagegenerator.infrastructure.imagegen;

import com.atlasdeals.imagegenerator.application.port.AiImageGenerator;
import com.atlasdeals.imagegenerator.domain.model.ImageStatus;
import com.atlasdeals.imagegenerator.domain.model.PromotionalImage;
import com.atlasdeals.imagegenerator.infrastructure.config.ImageProperties;
import com.atlasdeals.imagegenerator.infrastructure.storage.LocalImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class StabilityAiImageGenerator implements AiImageGenerator {

    private static final Logger log = LoggerFactory.getLogger(StabilityAiImageGenerator.class);

    private final ImageProperties imageProperties;
    private final LocalImageStorageService storageService;
    private final RestTemplate restTemplate;

    public StabilityAiImageGenerator(ImageProperties imageProperties,
                                     LocalImageStorageService storageService,
                                     RestTemplateBuilder restTemplateBuilder) {
        this.imageProperties = imageProperties;
        this.storageService = storageService;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2.0))
    public CompletableFuture<PromotionalImage> generatePromotionalImage(String productTitle, String storeName,
                                                                         BigDecimal price, BigDecimal originalPrice,
                                                                         String platform) {
        try {
            String apiKey = imageProperties.getAi().getStability().getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                return CompletableFuture.failedFuture(
                    new IllegalStateException("Stability AI API key not configured"));
            }

            String engine = imageProperties.getAi().getStability().getEngine();
            String prompt = buildPrompt(productTitle, storeName, price, originalPrice, platform);
            String negativePrompt = "text, watermark, signature, low quality, blurry, distorted";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            Map<String, Object> requestBody = Map.of(
                "text_prompts", java.util.List.of(
                    Map.of("text", prompt, "weight", 1.0),
                    Map.of("text", negativePrompt, "weight", -0.5)
                ),
                "cfg_scale", 7,
                "height", 1024,
                "width", 1024,
                "samples", 1,
                "steps", 30
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.stability.ai/v1/generation/" + engine + "/text-to-image",
                request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("Stability AI API returned: " + response.getStatusCode()));
            }

            var artifacts = (java.util.List<Map<String, Object>>) response.getBody().get("artifacts");
            if (artifacts == null || artifacts.isEmpty()) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("No artifacts in Stability AI response"));
            }

            String base64 = (String) artifacts.get(0).get("base64");
            if (base64 == null || base64.isBlank()) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("No base64 data in Stability AI response"));
            }

            java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
            byte[] imageData = decoder.decode(base64);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));

            String filename = UUID.randomUUID() + "_stability_" + platform.toLowerCase() + ".png";
            String localPath = storageService.save(imageData, filename);

            PromotionalImage promo = new PromotionalImage(UUID.randomUUID(), null, platform.toUpperCase());
            promo.setLocalPath(localPath);
            promo.setWidth(bufferedImage != null ? bufferedImage.getWidth() : 1024);
            promo.setHeight(bufferedImage != null ? bufferedImage.getHeight() : 1024);
            promo.setFileSize(imageData.length);
            promo.setFormat("PNG");
            promo.setStatus(ImageStatus.COMPLETED);
            promo.setAiGenerated(true);

            log.info("Stability AI generated image for '{}' on platform {}", productTitle, platform);

            return CompletableFuture.completedFuture(promo);
        } catch (Exception e) {
            log.error("Stability AI image generation failed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public boolean isAvailable() {
        String apiKey = imageProperties.getAi().getStability().getApiKey();
        return apiKey != null && !apiKey.isBlank() && imageProperties.getAi().isEnabled()
            && "stability".equalsIgnoreCase(imageProperties.getAi().getProvider());
    }

    private String buildPrompt(String productTitle, String storeName, BigDecimal price,
                                BigDecimal originalPrice, String platform) {
        StringBuilder sb = new StringBuilder();
        sb.append("Professional product promotional image. ");
        sb.append("Product: ").append(productTitle).append(". ");
        sb.append("Store: ").append(storeName).append(". ");
        sb.append("High quality, well-lit, modern design. ");
        sb.append("Clean background with professional composition. ");
        sb.append("Target platform: ").append(platform).append(".");
        return sb.toString();
    }
}
