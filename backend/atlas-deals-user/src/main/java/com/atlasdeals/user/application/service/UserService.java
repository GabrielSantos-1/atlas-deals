package com.atlasdeals.user.application.service;

import com.atlasdeals.user.domain.model.UserPreference;
import com.atlasdeals.user.domain.model.UserProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final EntityManager entityManager;

    public UserService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<UserProfile> getProfile(UUID userId) {
        TypedQuery<UserProfile> query = entityManager.createQuery(
            "SELECT u FROM UserProfile u WHERE u.userId = :userId", UserProfile.class);
        query.setParameter("userId", userId);
        return query.getResultList().stream().findFirst();
    }

    @Transactional
    public UserProfile updateProfile(UUID userId, String name, String email, String avatar,
                                      Integer minDiscountPercent, boolean notifyPush,
                                      boolean notifyEmail, boolean notifyWhatsApp,
                                      String telegramChatId, String phoneNumber) {
        UserProfile profile = getProfile(userId).orElseGet(() -> {
            UserProfile newProfile = new UserProfile(userId, name, email);
            entityManager.persist(newProfile);
            return newProfile;
        });

        if (name != null) profile.setName(name);
        if (email != null) profile.setEmail(email);
        if (avatar != null) profile.setAvatar(avatar);
        if (minDiscountPercent != null) profile.setMinDiscountPercent(minDiscountPercent);
        profile.setNotifyPush(notifyPush);
        profile.setNotifyEmail(notifyEmail);
        profile.setNotifyWhatsApp(notifyWhatsApp);
        if (telegramChatId != null) profile.setTelegramChatId(telegramChatId);
        if (phoneNumber != null) profile.setPhoneNumber(phoneNumber);

        entityManager.merge(profile);
        log.info("Updated profile for user: {}", userId);
        return profile;
    }

    @Transactional
    public void updatePreferences(UUID userId, String preferredCategories, String preferredBrands,
                                   String preferredStores) {
        UserProfile profile = getProfile(userId)
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        if (preferredCategories != null) profile.setPreferredCategories(preferredCategories);
        if (preferredBrands != null) profile.setPreferredBrands(preferredBrands);
        if (preferredStores != null) profile.setPreferredStores(preferredStores);
        entityManager.merge(profile);
        log.info("Updated preferences for user: {}", userId);
    }

    @Transactional
    public void addToFavorites(UUID userId, UUID productId) {
        UserPreference pref = findPreference(userId, "favorites")
            .orElse(new UserPreference(userId, "favorites", "[]"));
        try {
            String current = pref.getPreferenceValue();
            if (current == null || current.isBlank()) current = "[]";
            if (!current.contains(productId.toString())) {
                String updated = current.substring(0, current.length() - 1)
                    + (current.length() > 2 ? "," : "") + "\"" + productId + "\"]";
                pref.setPreferenceValue(updated);
                if (pref.getId() == null) entityManager.persist(pref);
                else entityManager.merge(pref);
            }
        } catch (Exception e) {
            log.error("Failed to add favorite: {}", e.getMessage());
        }
    }

    @Transactional
    public void removeFromFavorites(UUID userId, UUID productId) {
        findPreference(userId, "favorites").ifPresent(pref -> {
            String current = pref.getPreferenceValue();
            if (current != null) {
                String updated = current.replace("\"" + productId + "\"", "")
                    .replace(",]", "]").replace("[,", "[")
                    .replaceAll(",\\s*,", ",");
                pref.setPreferenceValue(updated);
                entityManager.merge(pref);
            }
        });
    }

    public List<String> getFavorites(UUID userId) {
        return findPreference(userId, "favorites")
            .map(pref -> {
                String val = pref.getPreferenceValue();
                if (val == null || val.isBlank()) return Collections.<String>emptyList();
                return Arrays.stream(val.replace("[", "").replace("]", "")
                        .replace("\"", "").split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            })
            .orElse(Collections.emptyList());
    }

    public List<String> getRecommendedCategories(UUID userId) {
        return getProfile(userId)
            .map(profile -> {
                String cats = profile.getPreferredCategories();
                if (cats == null || cats.isBlank()) return Collections.<String>emptyList();
                return Arrays.stream(cats.replace("[", "").replace("]", "")
                        .replace("\"", "").split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            })
            .orElse(Collections.emptyList());
    }

    private Optional<UserPreference> findPreference(UUID userId, String key) {
        TypedQuery<UserPreference> query = entityManager.createQuery(
            "SELECT p FROM UserPreference p WHERE p.userId = :userId AND p.preferenceKey = :key",
            UserPreference.class);
        query.setParameter("userId", userId);
        query.setParameter("key", key);
        return query.getResultList().stream().findFirst();
    }
}
