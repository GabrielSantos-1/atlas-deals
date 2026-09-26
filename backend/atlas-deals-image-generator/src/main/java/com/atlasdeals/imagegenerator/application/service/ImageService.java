package com.atlasdeals.imagegenerator.application.service;

import com.atlasdeals.imagegenerator.application.port.AiImageGenerator;
import com.atlasdeals.imagegenerator.application.port.ImageDownloader;
import com.atlasdeals.imagegenerator.application.port.ImageProcessor;
import com.atlasdeals.imagegenerator.domain.model.ImageStatus;
import com.atlasdeals.imagegenerator.domain.model.PlatformImageSpec;
import com.atlasdeals.imagegenerator.domain.model.ProductImage;
import com.atlasdeals.imagegenerator.domain.model.PromotionalImage;
import com.atlasdeals.imagegenerator.infrastructure.config.ImageProperties;
import com.atlasdeals.imagegenerator.infrastructure.messaging.ImageEventPublisher;
import com.atlasdeals.imagegenerator.infrastructure.storage.LocalImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import com.atlasdeals.common.domain.ProductId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    private final ImageDownloader imageDownloader;
    private final ImageProcessor imageProcessor;
    private final AiImageGenerator aiImageGenerator;
    private final LocalImageStorageService storageService;
    private final ImageOverlayEngine overlayEngine;
    private final ImageEventPublisher eventPublisher;
    private final ImageProperties imageProperties;

    private final Map<ProductId, ProductImage> productImageStore = new ConcurrentHashMap<>();
    private final Map<String, PromotionalImage> promotionalImageStore = new ConcurrentHashMap<>();

    public ImageService(ImageDownloader imageDownloader,
                        ImageProcessor imageProcessor,
                        AiImageGenerator aiImageGenerator,
                        LocalImageStorageService storageService,
                        ImageOverlayEngine overlayEngine,
                        ImageEventPublisher eventPublisher,
                        ImageProperties imageProperties) {
        this.imageDownloader = imageDownloader;
        this.imageProcessor = imageProcessor;
        this.aiImageGenerator = aiImageGenerator;
        this.storageService = storageService;
        this.overlayEngine = overlayEngine;
        this.eventPublisher = eventPublisher;
        this.imageProperties = imageProperties;
    }

    public CompletableFuture<ProductImage> downloadProductImage(ProductId productId, String imageUrl, String storeName) {
        log.info("Downloading product image for product {} from {}", productId, imageUrl);

        ProductImage pending = new ProductImage(productId.getValue(), imageUrl, storeName);
        pending.setStatus(ImageStatus.DOWNLOADING);
        productImageStore.put(productId, pending);

        return imageDownloader.download(imageUrl, productId, storeName)
            .thenApply(downloaded -> {
                downloaded.setStatus(ImageStatus.DOWNLOADED);
                productImageStore.put(productId, downloaded);
                log.info("Successfully downloaded image for product {}: {}x{} {} bytes",
                    productId, downloaded.getWidth(), downloaded.getHeight(), downloaded.getFileSize());
                return downloaded;
            })
            .exceptionally(ex -> {
                log.error("Failed to download image for product {}: {}", productId, ex.getMessage());
                pending.setStatus(ImageStatus.FAILED);
                pending.setErrorMessage(ex.getMessage());
                productImageStore.put(productId, pending);
                return pending;
            });
    }

    public List<PromotionalImage> generatePromotionalImages(ProductId productId, String title, String storeName,
                                                             BigDecimal currentPrice, BigDecimal originalPrice) {
        ProductImage productImage = productImageStore.get(productId);
        if (productImage == null || productImage.getStatus() != ImageStatus.DOWNLOADED) {
            log.warn("No downloaded product image found for product {}, attempting fallback", productId);
            return List.of();
        }

        BigDecimal discountPercent = BigDecimal.ZERO;
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0
            && currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            discountPercent = BigDecimal.ONE.subtract(
                currentPrice.divide(originalPrice, 4, RoundingMode.HALF_UP)
            ).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        }

        List<PromotionalImage> results = new ArrayList<>();
        List<PlatformImageSpec> specs = List.of(
            PlatformImageSpec.TELEGRAM,
            PlatformImageSpec.INSTAGRAM_POST,
            PlatformImageSpec.INSTAGRAM_STORY,
            PlatformImageSpec.WHATSAPP,
            PlatformImageSpec.SITE,
            PlatformImageSpec.FACEBOOK,
            PlatformImageSpec.TWITTER,
            PlatformImageSpec.DISCORD
        );

        for (PlatformImageSpec spec : specs) {
            try {
                Map<String, String> overlayData = overlayEngine.buildOverlayData(
                    title, storeName, currentPrice, originalPrice, discountPercent, spec.platform());

                PromotionalImage promo = generateForPlatform(productImage, spec, overlayData);
                results.add(promo);

                eventPublisher.publishImageGenerated(
                    productId, spec.platform(), promo.getLocalPath(),
                    promo.getWidth(), promo.getHeight(), promo.getFormat());

                log.info("Generated {} image for product {} ({}x{})",
                    spec.platform(), productId, spec.width(), spec.height());
            } catch (Exception e) {
                log.error("Failed to generate {} image for product {}: {}",
                    spec.platform(), productId, e.getMessage(), e);
            }
        }

        return results;
    }

    private PromotionalImage generateForPlatform(ProductImage productImage, PlatformImageSpec spec,
                                                  Map<String, String> overlayData) throws Exception {
        byte[] imageData = storageService.load(productImage.getLocalPath());
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageData));
        if (original == null) {
            throw new IllegalStateException("Failed to read image: " + productImage.getLocalPath());
        }

        BufferedImage processed = overlayEngine.applyOverlay(original, overlayData, spec.width(), spec.height());

        String filename = productImage.getProductId() + "_" + spec.platform().toLowerCase() + "." + spec.format().toLowerCase();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(processed, spec.format(), baos);
        byte[] resultData = baos.toByteArray();

        String localPath = storageService.save(resultData, filename);

        PromotionalImage promo = new PromotionalImage(
            productImage.getProductId(), productImage.getId(), spec.platform());
        promo.setLocalPath(localPath);
        promo.setWidth(spec.width());
        promo.setHeight(spec.height());
        promo.setFileSize(resultData.length);
        promo.setFormat(spec.format());
        promo.setStatus(ImageStatus.COMPLETED);

        String key = productImage.getProductId() + ":" + spec.platform();
        promotionalImageStore.put(key, promo);

        return promo;
    }

    public Optional<PromotionalImage> getImageForPlatform(ProductId productId, String platform) {
        String key = productId + ":" + platform.toUpperCase();
        PromotionalImage promo = promotionalImageStore.get(key);
        if (promo != null) return Optional.of(promo);

        ProductImage productImage = productImageStore.get(productId);
        if (productImage != null) {
            promo = promotionalImageStore.values().stream()
                .filter(p -> p.getProductId().equals(productId)
                    && p.getPlatform().equalsIgnoreCase(platform))
                .findFirst().orElse(null);
            if (promo != null) return Optional.of(promo);
        }
        return Optional.empty();
    }

    public Optional<ProductImage> getProductImage(UUID productId) {
        return Optional.ofNullable(productImageStore.get(productId));
    }

    public List<PromotionalImage> getAllImagesForProduct(UUID productId) {
        return promotionalImageStore.values().stream()
            .filter(p -> p.getProductId().equals(productId))
            .toList();
    }

    public void processQueue() {
        log.info("Processing image generation queue - no persistent queue implemented, using event-driven approach");
    }

    public CompletableFuture<PromotionalImage> generateAiImage(UUID productId, String title, String storeName,
                                                                 BigDecimal price, BigDecimal originalPrice,
                                                                 String platform) {
        if (!imageProperties.getAi().isEnabled() || !aiImageGenerator.isAvailable()) {
            log.warn("AI image generation is not available, skipping for product {}", productId);
            return CompletableFuture.failedFuture(new IllegalStateException("AI image generation not available"));
        }

        return aiImageGenerator.generatePromotionalImage(title, storeName, price, originalPrice, platform)
            .thenApply(promo -> {
                String key = productId + ":" + platform.toUpperCase();
                promotionalImageStore.put(key, promo);
                return promo;
            });
    }
}
