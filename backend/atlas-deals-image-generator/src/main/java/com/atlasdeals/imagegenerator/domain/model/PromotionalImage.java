package com.atlasdeals.imagegenerator.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotional_images")
public class PromotionalImage {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_image_id")
    private UUID productImageId;

    @Column(nullable = false)
    private String platform;

    @Column(name = "local_path", columnDefinition = "text")
    private String localPath;

    @Column(name = "width")
    private int width;

    @Column(name = "height")
    private int height;

    @Column(name = "file_size")
    private long fileSize;

    @Column(name = "format")
    private String format;

    @Column(name = "overlay_text", columnDefinition = "text")
    private String overlayText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageStatus status;

    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PromotionalImage() {}

    public PromotionalImage(UUID productId, UUID productImageId, String platform) {
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.productImageId = productImageId;
        this.platform = platform;
        this.status = ImageStatus.PENDING;
        this.aiGenerated = false;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (id == null) id = UUID.randomUUID();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public UUID getProductImageId() { return productImageId; }
    public void setProductImageId(UUID productImageId) { this.productImageId = productImageId; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getOverlayText() { return overlayText; }
    public void setOverlayText(String overlayText) { this.overlayText = overlayText; }

    public ImageStatus getStatus() { return status; }
    public void setStatus(ImageStatus status) { this.status = status; }

    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
