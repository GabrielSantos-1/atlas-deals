package com.atlasdeals.imagegenerator.infrastructure.web;

import com.atlasdeals.imagegenerator.application.service.ImageService;
import com.atlasdeals.imagegenerator.domain.model.PlatformImageSpec;
import com.atlasdeals.imagegenerator.domain.model.ProductImage;
import com.atlasdeals.imagegenerator.domain.model.PromotionalImage;
import com.atlasdeals.imagegenerator.infrastructure.storage.LocalImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;
    private final LocalImageStorageService storageService;

    public ImageController(ImageService imageService, LocalImageStorageService storageService) {
        this.imageService = imageService;
        this.storageService = storageService;
    }

    @PostMapping("/generate/{productId}")
    public ResponseEntity<?> triggerImageGeneration(
            @PathVariable UUID productId,
            @RequestBody GenerateRequest request) {
        log.info("Triggering image generation for product {}", productId);

        CompletableFuture<ProductImage> downloadFuture = imageService.downloadProductImage(
            productId, request.imageUrl(), request.storeName());

        try {
            ProductImage productImage = downloadFuture.join();
            if (productImage.getErrorMessage() != null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to download image",
                    "message", productImage.getErrorMessage()
                ));
            }

            List<PromotionalImage> promos = imageService.generatePromotionalImages(
                productId, request.title(), request.storeName(),
                request.currentPrice(), request.originalPrice());

            return ResponseEntity.ok(Map.of(
                "productImage", Map.of(
                    "id", productImage.getId(),
                    "width", productImage.getWidth(),
                    "height", productImage.getHeight(),
                    "format", productImage.getFormat(),
                    "localPath", productImage.getLocalPath()
                ),
                "promotionalImages", promos.stream().map(p -> Map.of(
                    "id", p.getId(),
                    "platform", p.getPlatform(),
                    "width", p.getWidth(),
                    "height", p.getHeight(),
                    "format", p.getFormat(),
                    "localPath", p.getLocalPath(),
                    "status", p.getStatus()
                )).toList()
            ));
        } catch (Exception e) {
            log.error("Image generation failed for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Image generation failed",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductImage(@PathVariable UUID productId) {
        return imageService.getProductImage(productId)
            .map(image -> ResponseEntity.ok(Map.of(
                "id", image.getId(),
                "productId", image.getProductId(),
                "originalUrl", image.getOriginalUrl(),
                "localPath", image.getLocalPath(),
                "width", image.getWidth(),
                "height", image.getHeight(),
                "format", image.getFormat(),
                "fileSize", image.getFileSize(),
                "status", image.getStatus()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}/platform/{platform}")
    public ResponseEntity<?> getImageForPlatform(
            @PathVariable UUID productId,
            @PathVariable String platform) {
        return imageService.getImageForPlatform(productId, platform)
            .map(image -> ResponseEntity.ok(Map.of(
                "id", image.getId(),
                "productId", image.getProductId(),
                "platform", image.getPlatform(),
                "localPath", image.getLocalPath(),
                "width", image.getWidth(),
                "height", image.getHeight(),
                "format", image.getFormat(),
                "fileSize", image.getFileSize(),
                "status", image.getStatus(),
                "aiGenerated", image.isAiGenerated()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}/all")
    public ResponseEntity<?> getAllImages(@PathVariable UUID productId) {
        List<PromotionalImage> images = imageService.getAllImagesForProduct(productId);
        return ResponseEntity.ok(images.stream().map(p -> Map.of(
            "id", p.getId(),
            "productId", p.getProductId(),
            "platform", p.getPlatform(),
            "localPath", p.getLocalPath(),
            "width", p.getWidth(),
            "height", p.getHeight(),
            "format", p.getFormat(),
            "fileSize", p.getFileSize(),
            "status", p.getStatus(),
            "aiGenerated", p.isAiGenerated()
        )).toList());
    }

    @PostMapping("/regenerate/{imageId}")
    public ResponseEntity<?> regenerateImage(@PathVariable UUID imageId) {
        log.info("Regenerate request for image {}", imageId);
        return ResponseEntity.ok(Map.of(
            "message", "Regeneration initiated for image " + imageId,
            "imageId", imageId
        ));
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<byte[]> getImageFile(@PathVariable String filename) {
        try {
            String path = filename;
            byte[] data = storageService.load(path);
            String format = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1) : "jpg";
            MediaType mediaType = switch (format.toLowerCase()) {
                case "png" -> MediaType.IMAGE_PNG;
                case "webp" -> MediaType.valueOf("image/webp");
                default -> MediaType.IMAGE_JPEG;
            };
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(data);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public record GenerateRequest(
        String imageUrl,
        String storeName,
        String title,
        BigDecimal currentPrice,
        BigDecimal originalPrice
    ) {}
}
