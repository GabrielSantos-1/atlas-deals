package com.atlasdeals.imagegenerator.application.port;

import com.atlasdeals.imagegenerator.domain.model.ProductImage;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ImageDownloader {
    CompletableFuture<ProductImage> download(String imageUrl, UUID productId, String storeName);
    boolean supports(String url);
}
