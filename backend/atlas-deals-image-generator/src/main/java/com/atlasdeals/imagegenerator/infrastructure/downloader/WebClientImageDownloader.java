package com.atlasdeals.imagegenerator.infrastructure.downloader;

import com.atlasdeals.imagegenerator.application.port.ImageDownloader;
import com.atlasdeals.imagegenerator.domain.model.ImageStatus;
import com.atlasdeals.imagegenerator.domain.model.ProductImage;
import com.atlasdeals.imagegenerator.infrastructure.config.ImageProperties;
import com.atlasdeals.imagegenerator.infrastructure.storage.LocalImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class WebClientImageDownloader implements ImageDownloader {

    private static final Logger log = LoggerFactory.getLogger(WebClientImageDownloader.class);

    private static final String[] SUPPORTED_FORMATS = {"image/jpeg", "image/png", "image/webp"};

    private final WebClient webClient;
    private final LocalImageStorageService storageService;
    private final ImageProperties imageProperties;

    public WebClientImageDownloader(WebClient.Builder webClientBuilder,
                                    LocalImageStorageService storageService,
                                    ImageProperties imageProperties) {
        this.storageService = storageService;
        this.imageProperties = imageProperties;
        this.webClient = webClientBuilder
            .baseUrl("")
            .defaultHeader("User-Agent", imageProperties.getDownload().getUserAgent())
            .build();
    }

    @Override
    public CompletableFuture<ProductImage> download(String imageUrl, UUID productId, String storeName) {
        ProductImage productImage = new ProductImage(productId, imageUrl, storeName);
        productImage.setStatus(ImageStatus.DOWNLOADING);

        return webClient.get()
            .uri(URI.create(imageUrl))
            .headers(headers -> headers.set("Accept", String.join(", ", SUPPORTED_FORMATS)))
            .retrieve()
            .bodyToMono(byte[].class)
            .timeout(Duration.ofMillis(imageProperties.getDownload().getTimeout()))
            .flatMap(data -> validateAndProcessImage(data, productImage))
            .doOnSuccess(p -> log.info("Successfully downloaded image for product {}: {}x{}",
                productId, p.getWidth(), p.getHeight()))
            .doOnError(e -> log.error("Failed to download image from {}: {}", imageUrl, e.getMessage()))
            .onErrorResume(e -> {
                productImage.setStatus(ImageStatus.FAILED);
                productImage.setErrorMessage(e.getMessage());
                return Mono.just(productImage);
            })
            .toFuture();
    }

    private Mono<ProductImage> validateAndProcessImage(byte[] data, ProductImage productImage) {
        try {
            String contentType = detectImageType(data);
            if (contentType == null) {
                return Mono.error(new IllegalArgumentException("Unsupported or invalid image format"));
            }
            productImage.setFormat(contentType.toUpperCase());

            java.awt.image.BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
            if (bufferedImage == null) {
                return Mono.error(new IllegalArgumentException("Failed to decode image"));
            }

            int maxDim = imageProperties.getProcessing().getMaxDimension();
            if (bufferedImage.getWidth() > maxDim || bufferedImage.getHeight() > maxDim) {
                return Mono.error(new IllegalArgumentException(
                    "Image dimensions too large: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight()));
            }

            productImage.setWidth(bufferedImage.getWidth());
            productImage.setHeight(bufferedImage.getHeight());
            productImage.setFileSize(data.length);

            String extension = switch (contentType.toLowerCase()) {
                case "jpeg", "jpg" -> "jpg";
                case "png" -> "png";
                case "webp" -> "webp";
                default -> "jpg";
            };
            String filename = productImage.getProductId() + "_original." + extension;
            String localPath = storageService.save(data, filename);

            productImage.setLocalPath(localPath);
            productImage.setStatus(ImageStatus.DOWNLOADED);

            log.info("Image processed: {}x{} format={} path={}",
                productImage.getWidth(), productImage.getHeight(),
                productImage.getFormat(), localPath);

            return Mono.just(productImage);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private String detectImageType(byte[] data) {
        if (data == null || data.length < 4) return null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ImageInputStream iis = ImageIO.createImageInputStream(bais)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                String format = reader.getFormatName().toLowerCase();
                if ("jpeg".equals(format) || "jpg".equals(format)) return "JPEG";
                if ("png".equals(format)) return "PNG";
                if ("webp".equals(format)) return "WebP";
            }
        } catch (Exception ignored) {}

        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) return "JPEG";
        if (data[0] == (byte) 0x89 && data[1] == (byte) 0x50 && data[2] == (byte) 0x4E && data[3] == (byte) 0x47) return "PNG";
        if (data[0] == (byte) 0x52 && data[1] == (byte) 0x49 && data[2] == (byte) 0x46 && data[3] == (byte) 0x46) return "WebP";

        return null;
    }

    @Override
    public boolean supports(String url) {
        if (url == null || url.isBlank()) return false;
        String lower = url.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
