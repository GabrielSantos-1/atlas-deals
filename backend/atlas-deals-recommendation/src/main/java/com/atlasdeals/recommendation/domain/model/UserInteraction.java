package com.atlasdeals.recommendation.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_interactions")
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "interaction_type", nullable = false, length = 20)
    private String interactionType;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UserInteraction() {
    }

    public UserInteraction(UUID userId, UUID productId, String interactionType, double score) {
        this.userId = userId;
        this.productId = productId;
        this.interactionType = interactionType;
        this.score = score;
        this.timestamp = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getInteractionType() { return interactionType; }
    public void setInteractionType(String interactionType) { this.interactionType = interactionType; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Instant getCreatedAt() { return createdAt; }

    public enum InteractionType {
        VIEW(1.0),
        CLICK(2.0),
        CONVERSION(5.0),
        FAVORITE(3.0),
        SHARE(4.0);

        private final double defaultScore;

        InteractionType(double defaultScore) {
            this.defaultScore = defaultScore;
        }

        public double getDefaultScore() {
            return defaultScore;
        }
    }
}
