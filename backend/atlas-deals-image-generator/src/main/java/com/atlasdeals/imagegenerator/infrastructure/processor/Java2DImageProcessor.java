package com.atlasdeals.imagegenerator.infrastructure.processor;

import com.atlasdeals.imagegenerator.application.port.ImageProcessor;
import com.atlasdeals.imagegenerator.application.service.ImageOverlayEngine;
import com.atlasdeals.imagegenerator.domain.model.ImageStatus;
import com.atlasdeals.imagegenerator.domain.model.PlatformImageSpec;
import com.atlasdeals.imagegenerator.domain.model.ProductImage;
import com.atlasdeals.imagegenerator.domain.model.PromotionalImage;
import com.atlasdeals.imagegenerator.infrastructure.storage.LocalImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Set;

@Component
public class Java2DImageProcessor implements ImageProcessor {

    private static final Logger log = LoggerFactory.getLogger(Java2DImageProcessor.class);
    private static final Set<String> SUPPORTED_FORMATS = Set.of("JPEG", "JPG", "PNG", "WEBP");

    private final LocalImageStorageService storageService;
    private final ImageOverlayEngine overlayEngine;

    public Java2DImageProcessor(LocalImageStorageService storageService, ImageOverlayEngine overlayEngine) {
        this.storageService = storageService;
        this.overlayEngine = overlayEngine;
    }

    @Override
    public PromotionalImage generatePromotionalImage(ProductImage original, PlatformImageSpec spec,
                                                      Map<String, String> overlayData) {
        try {
            byte[] imageData = storageService.load(original.getLocalPath());
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageData));
            if (source == null) {
                throw new IllegalStateException("Cannot read source image: " + original.getLocalPath());
            }

            BufferedImage processed = overlayEngine.applyOverlay(source, overlayData, spec.width(), spec.height());

            String format = spec.format().toLowerCase();
            if ("jpeg".equals(format)) format = "jpg";

            String filename = original.getProductId() + "_" + spec.platform().toLowerCase() + "." + format;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processed, spec.format(), baos);
            byte[] resultData = baos.toByteArray();

            String localPath = storageService.save(resultData, filename);

            PromotionalImage promo = new PromotionalImage(
                original.getProductId(), original.getId(), spec.platform());
            promo.setLocalPath(localPath);
            promo.setWidth(spec.width());
            promo.setHeight(spec.height());
            promo.setFileSize(resultData.length);
            promo.setFormat(spec.format());
            promo.setStatus(ImageStatus.COMPLETED);
            promo.setAiGenerated(false);

            log.info("Generated promotional image for {}: {}x{} at {}",
                spec.platform(), spec.width(), spec.height(), localPath);

            return promo;
        } catch (Exception e) {
            log.error("Failed to generate promotional image for {}: {}", spec.platform(), e.getMessage(), e);
            PromotionalImage failed = new PromotionalImage(
                original.getProductId(), original.getId(), spec.platform());
            failed.setStatus(ImageStatus.FAILED);
            failed.setErrorMessage(e.getMessage());
            return failed;
        }
    }

    @Override
    public PromotionalImage resize(ProductImage image, int width, int height) {
        try {
            byte[] imageData = storageService.load(image.getLocalPath());
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageData));
            if (source == null) {
                throw new IllegalStateException("Cannot read source image: " + image.getLocalPath());
            }

            BufferedImage resized = overlayEngine.resize(source, width, height);

            String filename = image.getProductId() + "_resized_" + width + "x" + height + ".jpg";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resized, "JPEG", baos);
            byte[] resultData = baos.toByteArray();

            String localPath = storageService.save(resultData, filename);

            PromotionalImage promo = new PromotionalImage(image.getProductId(), image.getId(), "RESIZED");
            promo.setLocalPath(localPath);
            promo.setWidth(width);
            promo.setHeight(height);
            promo.setFileSize(resultData.length);
            promo.setFormat("JPEG");
            promo.setStatus(ImageStatus.COMPLETED);

            return promo;
        } catch (Exception e) {
            log.error("Failed to resize image: {}", e.getMessage(), e);
            PromotionalImage failed = new PromotionalImage(image.getProductId(), image.getId(), "RESIZED");
            failed.setStatus(ImageStatus.FAILED);
            failed.setErrorMessage(e.getMessage());
            return failed;
        }
    }

    @Override
    public PromotionalImage addOverlay(ProductImage image, Map<String, String> textElements) {
        PlatformImageSpec spec = PlatformImageSpec.fromPlatform(
            textElements.getOrDefault("platform", "SITE"));
        return generatePromotionalImage(image, spec, textElements);
    }

    @Override
    public boolean supportsFormat(String format) {
        return SUPPORTED_FORMATS.contains(format.toUpperCase());
    }
}
