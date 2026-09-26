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
import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class DallEImageGenerator implements AiImageGenerator {

    private static final Logger log = LoggerFactory.getLogger(DallEImageGenerator.class);

    private final ImageProperties imageProperties;
    private final LocalImageStorageService storageService;
    private final RestTemplate restTemplate;

    public DallEImageGenerator(ImageProperties imageProperties,
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
            String apiKey = imageProperties.getAi().getOpenai().getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                return CompletableFuture.failedFuture(
                    new IllegalStateException("OpenAI API key not configured"));
            }

            String prompt = buildPrompt(productTitle, storeName, price, originalPrice, platform);
            String size = imageProperties.getAi().getOpenai().getSize();
            String model = imageProperties.getAi().getOpenai().getModel();
            String quality = imageProperties.getAi().getOpenai().getQuality();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", prompt,
                "n", 1,
                "size", size,
                "quality", quality,
                "response_format", "b64_json"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/images/generations",
                request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("DALL-E API returned: " + response.getStatusCode()));
            }

            var dataList = (java.util.List<Map<String, Object>>) response.getBody().get("data");
            if (dataList == null || dataList.isEmpty()) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("No image data in DALL-E response"));
            }

            String b64Json = (String) dataList.get(0).get("b64_json");
            if (b64Json == null || b64Json.isBlank()) {
                return CompletableFuture.failedFuture(
                    new RuntimeException("No b64_json in DALL-E response"));
            }

            byte[] imageData = Base64.getDecoder().decode(b64Json);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageData));

            String filename = UUID.randomUUID() + "_ai_" + platform.toLowerCase() + ".png";
            String localPath = storageService.save(imageData, filename);

            PromotionalImage promo = new PromotionalImage(UUID.randomUUID(), null, platform.toUpperCase());
            promo.setLocalPath(localPath);
            promo.setWidth(bufferedImage != null ? bufferedImage.getWidth() : 1024);
            promo.setHeight(bufferedImage != null ? bufferedImage.getHeight() : 1024);
            promo.setFileSize(imageData.length);
            promo.setFormat("PNG");
            promo.setStatus(ImageStatus.COMPLETED);
            promo.setAiGenerated(true);

            log.info("DALL-E generated image for '{}' on platform {}", productTitle, platform);

            return CompletableFuture.completedFuture(promo);
        } catch (Exception e) {
            log.error("DALL-E image generation failed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public boolean isAvailable() {
        String apiKey = imageProperties.getAi().getOpenai().getApiKey();
        return apiKey != null && !apiKey.isBlank() && imageProperties.getAi().isEnabled();
    }

    private String buildPrompt(String productTitle, String storeName, BigDecimal price,
                                BigDecimal originalPrice, String platform) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create a professional promotional image for a product deal. ");
        sb.append("Product: ").append(productTitle).append(". ");
        sb.append("Store: ").append(storeName).append(". ");
        if (price != null) sb.append("Current price: R$").append(price).append(". ");
        if (originalPrice != null) sb.append("Original price: R$").append(originalPrice).append(". ");
        sb.append("The image should be clean, well-lit, with the product prominently featured. ");
        sb.append("Use modern design with a gradient background. ");
        sb.append("Do not add text overlays - they will be added separately. ");
        sb.append("Target platform: ").append(platform).append(".");
        return sb.toString();
    }
}
