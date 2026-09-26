package com.atlasdeals.imagegenerator.domain.model;

public record PlatformImageSpec(
    String platform,
    int width,
    int height,
    long maxFileSizeBytes,
    String format,
    String description
) {
    public static final PlatformImageSpec TELEGRAM = new PlatformImageSpec(
        "TELEGRAM", 1280, 720, 10L * 1024 * 1024, "JPEG", "Telegram feed image");

    public static final PlatformImageSpec INSTAGRAM_POST = new PlatformImageSpec(
        "INSTAGRAM_POST", 1080, 1080, 10L * 1024 * 1024, "JPEG", "Instagram post (square)");

    public static final PlatformImageSpec INSTAGRAM_STORY = new PlatformImageSpec(
        "INSTAGRAM_STORY", 1080, 1920, 10L * 1024 * 1024, "JPEG", "Instagram story (portrait)");

    public static final PlatformImageSpec WHATSAPP = new PlatformImageSpec(
        "WHATSAPP", 800, 800, 5L * 1024 * 1024, "JPEG", "WhatsApp share image");

    public static final PlatformImageSpec SITE = new PlatformImageSpec(
        "SITE", 1200, 630, 2L * 1024 * 1024, "JPEG", "Open Graph / site preview");

    public static final PlatformImageSpec FACEBOOK = new PlatformImageSpec(
        "FACEBOOK", 1200, 630, 8L * 1024 * 1024, "JPEG", "Facebook share image");

    public static final PlatformImageSpec TWITTER = new PlatformImageSpec(
        "TWITTER", 1200, 675, 5L * 1024 * 1024, "JPEG", "Twitter/X card image");

    public static final PlatformImageSpec DISCORD = new PlatformImageSpec(
        "DISCORD", 512, 512, 1L * 1024 * 1024, "PNG", "Discord embed image");

    public static PlatformImageSpec fromPlatform(String platform) {
        return switch (platform.toUpperCase()) {
            case "TELEGRAM" -> TELEGRAM;
            case "INSTAGRAM_POST" -> INSTAGRAM_POST;
            case "INSTAGRAM_STORY" -> INSTAGRAM_STORY;
            case "WHATSAPP" -> WHATSAPP;
            case "SITE" -> SITE;
            case "FACEBOOK" -> FACEBOOK;
            case "TWITTER" -> TWITTER;
            case "DISCORD" -> DISCORD;
            default -> throw new IllegalArgumentException("Unknown platform: " + platform);
        };
    }
}
