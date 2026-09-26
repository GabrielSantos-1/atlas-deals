package com.atlasdeals.aiengine.domain.model;

import com.atlasdeals.aiengine.domain.GenerationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ai_contents")
public class AiContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(name = "seo_description", columnDefinition = "text")
    private String seoDescription;

    @Column(columnDefinition = "text")
    private String hashtags;

    @Column(length = 255)
    private String category;

    @Column(columnDefinition = "text")
    private String tags;

    @Column(name = "opportunity_level", length = 50)
    private String opportunityLevel;

    @Column(name = "technical_summary", columnDefinition = "text")
    private String technicalSummary;

    @Column(columnDefinition = "text")
    private String pros;

    @Column(columnDefinition = "text")
    private String cons;

    @Column(name = "recommended_profile", columnDefinition = "text")
    private String recommendedProfile;

    @Column(name = "comparison_with_history", columnDefinition = "text")
    private String comparisonWithHistory;

    @Column(name = "comparison_with_competitors", columnDefinition = "text")
    private String comparisonWithCompetitors;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_by_platform", columnDefinition = "jsonb")
    private Map<String, String> contentByPlatform = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "generation_status", nullable = false, length = 50)
    private GenerationStatus generationStatus = GenerationStatus.PENDING;

    @Column(name = "ai_model", length = 100)
    private String aiModel;

    @Column(name = "tokens_used")
    private int tokensUsed;

    @Column(name = "prompt_tokens")
    private int promptTokens;

    @Column(name = "completion_tokens")
    private int completionTokens;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AiContent() {}

    public AiContent(UUID productId) {
        this.productId = productId;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSeoDescription() { return seoDescription; }
    public void setSeoDescription(String seoDescription) { this.seoDescription = seoDescription; }
    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getOpportunityLevel() { return opportunityLevel; }
    public void setOpportunityLevel(String opportunityLevel) { this.opportunityLevel = opportunityLevel; }
    public String getTechnicalSummary() { return technicalSummary; }
    public void setTechnicalSummary(String technicalSummary) { this.technicalSummary = technicalSummary; }
    public String getPros() { return pros; }
    public void setPros(String pros) { this.pros = pros; }
    public String getCons() { return cons; }
    public void setCons(String cons) { this.cons = cons; }
    public String getRecommendedProfile() { return recommendedProfile; }
    public void setRecommendedProfile(String recommendedProfile) { this.recommendedProfile = recommendedProfile; }
    public String getComparisonWithHistory() { return comparisonWithHistory; }
    public void setComparisonWithHistory(String comparisonWithHistory) { this.comparisonWithHistory = comparisonWithHistory; }
    public String getComparisonWithCompetitors() { return comparisonWithCompetitors; }
    public void setComparisonWithCompetitors(String comparisonWithCompetitors) { this.comparisonWithCompetitors = comparisonWithCompetitors; }
    public Map<String, String> getContentByPlatform() { return contentByPlatform; }
    public void setContentByPlatform(Map<String, String> contentByPlatform) { this.contentByPlatform = contentByPlatform; }
    public GenerationStatus getGenerationStatus() { return generationStatus; }
    public void setGenerationStatus(GenerationStatus generationStatus) { this.generationStatus = generationStatus; }
    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }
    public int getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }
    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
