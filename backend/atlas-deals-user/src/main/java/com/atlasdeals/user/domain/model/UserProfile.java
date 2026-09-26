package com.atlasdeals.user.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 500)
    private String avatar;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_categories", columnDefinition = "jsonb")
    private String preferredCategories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_brands", columnDefinition = "jsonb")
    private String preferredBrands;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_stores", columnDefinition = "jsonb")
    private String preferredStores;

    @Column(name = "min_discount_percent")
    private Integer minDiscountPercent;

    @Column(name = "notify_push", nullable = false)
    private boolean notifyPush;

    @Column(name = "notify_email", nullable = false)
    private boolean notifyEmail;

    @Column(name = "notify_whatsapp", nullable = false)
    private boolean notifyWhatsApp;

    @Column(name = "telegram_chat_id", length = 100)
    private String telegramChatId;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserProfile() {
    }

    public UserProfile(UUID userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.notifyPush = true;
        this.notifyEmail = true;
        this.notifyWhatsApp = false;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getPreferredCategories() { return preferredCategories; }
    public void setPreferredCategories(String preferredCategories) { this.preferredCategories = preferredCategories; }
    public String getPreferredBrands() { return preferredBrands; }
    public void setPreferredBrands(String preferredBrands) { this.preferredBrands = preferredBrands; }
    public String getPreferredStores() { return preferredStores; }
    public void setPreferredStores(String preferredStores) { this.preferredStores = preferredStores; }
    public Integer getMinDiscountPercent() { return minDiscountPercent; }
    public void setMinDiscountPercent(Integer minDiscountPercent) { this.minDiscountPercent = minDiscountPercent; }
    public boolean isNotifyPush() { return notifyPush; }
    public void setNotifyPush(boolean notifyPush) { this.notifyPush = notifyPush; }
    public boolean isNotifyEmail() { return notifyEmail; }
    public void setNotifyEmail(boolean notifyEmail) { this.notifyEmail = notifyEmail; }
    public boolean isNotifyWhatsApp() { return notifyWhatsApp; }
    public void setNotifyWhatsApp(boolean notifyWhatsApp) { this.notifyWhatsApp = notifyWhatsApp; }
    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
