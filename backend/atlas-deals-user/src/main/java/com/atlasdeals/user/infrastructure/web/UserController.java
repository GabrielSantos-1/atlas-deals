package com.atlasdeals.user.infrastructure.web;

import com.atlasdeals.user.application.service.UserService;
import com.atlasdeals.user.domain.model.UserProfile;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(@RequestParam UUID userId) {
        return userService.getProfile(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateProfile(@RequestParam UUID userId,
                                                      @RequestBody UpdateProfileRequest request) {
        UserProfile profile = userService.updateProfile(
            userId, request.name(), request.email(), request.avatar(),
            request.minDiscountPercent(), request.notifyPush(),
            request.notifyEmail(), request.notifyWhatsApp(),
            request.telegramChatId(), request.phoneNumber());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/preferences")
    public ResponseEntity<Void> updatePreferences(@RequestParam UUID userId,
                                                   @RequestBody UpdatePreferencesRequest request) {
        userService.updatePreferences(userId, request.preferredCategories(),
            request.preferredBrands(), request.preferredStores());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<String>> getFavorites(@RequestParam UUID userId) {
        return ResponseEntity.ok(userService.getFavorites(userId));
    }

    @PostMapping("/favorites/{productId}")
    public ResponseEntity<Void> addFavorite(@RequestParam UUID userId, @PathVariable UUID productId) {
        userService.addToFavorites(userId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites/{productId}")
    public ResponseEntity<Void> removeFavorite(@RequestParam UUID userId, @PathVariable UUID productId) {
        userService.removeFromFavorites(userId, productId);
        return ResponseEntity.noContent().build();
    }

    record UpdateProfileRequest(String name, String email, String avatar, Integer minDiscountPercent,
                                 boolean notifyPush, boolean notifyEmail, boolean notifyWhatsApp,
                                 String telegramChatId, String phoneNumber) {}

    record UpdatePreferencesRequest(String preferredCategories, String preferredBrands, String preferredStores) {}
}
