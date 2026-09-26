package com.atlasdeals.imagegenerator.application.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageOverlayEngine {

    private static final Map<String, ColorScheme> STORE_COLORS = new LinkedHashMap<>();
    static {
        STORE_COLORS.put("shopee", new ColorScheme(new Color(0xEE4D2D), new Color(0xFF6633), Color.WHITE));
        STORE_COLORS.put("amazon", new ColorScheme(new Color(0x232F3E), new Color(0xFF9900), Color.WHITE));
        STORE_COLORS.put("kabum", new ColorScheme(new Color(0x0051A0), new Color(0x0099FF), Color.WHITE));
        STORE_COLORS.put("magazineluiza", new ColorScheme(new Color(0x00A0DF), new Color(0x1F8E3E), Color.WHITE));
        STORE_COLORS.put("mercadolivre", new ColorScheme(new Color(0xFFE600), new Color(0x003B7A), new Color(0x333333)));
        STORE_COLORS.put("americanas", new ColorScheme(new Color(0xCC0000), new Color(0xFF3333), Color.WHITE));
        STORE_COLORS.put("aliexpress", new ColorScheme(new Color(0xFF4747), new Color(0x333333), Color.WHITE));
        STORE_COLORS.put("default", new ColorScheme(new Color(0x333333), new Color(0x666666), Color.WHITE));
    }

    private static final Color GRADIENT_TOP = new Color(0, 0, 0, 0);
    private static final Color GRADIENT_BOTTOM = new Color(0, 0, 0, 180);
    private static final Color DISCOUNT_BG = new Color(0xFF4444);
    private static final Color DISCOUNT_TEXT = Color.WHITE;

    public BufferedImage applyOverlay(BufferedImage source, Map<String, String> overlayData, int targetWidth, int targetHeight) {
        BufferedImage canvas = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);

            String storeName = overlayData.getOrDefault("storeName", "default").toLowerCase();
            ColorScheme scheme = STORE_COLORS.getOrDefault(storeName, STORE_COLORS.get("default"));

            drawGradientOverlay(g2d, targetWidth, targetHeight);
            drawStoreBadge(g2d, overlayData.getOrDefault("storeName", ""), scheme, targetWidth);
            drawProductTitle(g2d, overlayData.getOrDefault("title", ""), targetWidth);
            drawPriceOverlay(g2d, overlayData, targetWidth, targetHeight);
            drawDiscountBadge(g2d, overlayData, targetWidth, targetHeight);
            drawPlatformWatermark(g2d, overlayData.getOrDefault("platform", ""), targetWidth, targetHeight);

            return canvas;
        } finally {
            g2d.dispose();
        }
    }

    private void drawGradientOverlay(Graphics2D g2d, int width, int height) {
        int gradientHeight = height / 3;
        int gradientStart = height - gradientHeight;
        GradientPaint gradient = new GradientPaint(
            0, gradientStart, GRADIENT_TOP,
            0, height, GRADIENT_BOTTOM);
        g2d.setPaint(gradient);
        g2d.fillRect(0, gradientStart, width, gradientHeight);
    }

    private void drawStoreBadge(Graphics2D g2d, String storeName, ColorScheme scheme, int width) {
        if (storeName == null || storeName.isBlank()) return;

        String label = storeName.toUpperCase();
        int fontSize = Math.max(12, width / 60);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        int padding = 8;
        int textWidth = fm.stringWidth(label);
        int badgeWidth = textWidth + padding * 2;
        int badgeHeight = fm.getHeight() + padding;
        int x = 12;
        int y = 12;

        g2d.setColor(scheme.primary());
        g2d.fillRoundRect(x, y, badgeWidth, badgeHeight, 6, 6);
        g2d.setColor(scheme.text());
        g2d.drawString(label, x + padding, y + fm.getAscent() + padding / 2);
    }

    private void drawProductTitle(Graphics2D g2d, String title, int width) {
        if (title == null || title.isBlank()) return;

        int maxWidth = width - 40;
        int fontSize = Math.max(14, width / 40);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        String truncated = truncateText(title, fm, maxWidth);
        int textY = Math.min(60, width / 8);

        g2d.setColor(Color.WHITE);
        int shadowOffset = 1;
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.drawString(truncated, 21, textY + shadowOffset);
        g2d.setColor(Color.WHITE);
        g2d.drawString(truncated, 20, textY);
    }

    private void drawPriceOverlay(Graphics2D g2d, Map<String, String> overlayData, int width, int height) {
        String currentPrice = overlayData.getOrDefault("currentPrice", "");
        String originalPrice = overlayData.getOrDefault("originalPrice", "");
        int bottomAreaY = height - (height / 5);

        if (!currentPrice.isBlank()) {
            int priceFontSize = Math.max(28, width / 15);
            Font priceFont = new Font("Arial", Font.BOLD, priceFontSize);
            g2d.setFont(priceFont);
            FontMetrics fm = g2d.getFontMetrics();

            int priceX = 20;
            int priceY = bottomAreaY + fm.getAscent();

            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.drawString(currentPrice, priceX + 2, priceY + 2);
            g2d.setColor(new Color(0x00E676));
            g2d.drawString(currentPrice, priceX, priceY);
        }

        if (!originalPrice.isBlank()) {
            int origFontSize = Math.max(16, width / 28);
            Font origFont = new Font("Arial", Font.PLAIN, origFontSize);
            g2d.setFont(origFont);
            FontMetrics fm = g2d.getFontMetrics();

            int origX = 20;
            int origY = bottomAreaY - 8;

            g2d.setColor(new Color(180, 180, 180));
            int strikeY = origY - fm.getDescent() + fm.getAscent() / 2;
            g2d.drawString(originalPrice, origX, origY);
            int strikeWidth = fm.stringWidth(originalPrice);
            g2d.drawLine(origX, strikeY, origX + strikeWidth, strikeY);
        }
    }

    private void drawDiscountBadge(Graphics2D g2d, Map<String, String> overlayData, int width, int height) {
        String discountStr = overlayData.getOrDefault("discount", "");
        if (discountStr.isBlank()) return;

        int badgeSize = Math.max(50, width / 8);
        int x = width - badgeSize - 15;
        int y = 15;

        g2d.setColor(DISCOUNT_BG);
        g2d.fill(new Ellipse2D.Double(x, y, badgeSize, badgeSize));

        int fontSize = Math.max(12, badgeSize / 4);
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);
        g2d.setColor(DISCOUNT_TEXT);
        FontMetrics fm = g2d.getFontMetrics();

        String label = "-" + discountStr;
        if (!label.contains("%")) label += "%";
        int textX = x + (badgeSize - fm.stringWidth(label)) / 2;
        int textY = y + (badgeSize + fm.getAscent()) / 2 - 4;
        g2d.drawString(label, textX, textY);

        Font offFont = new Font("Arial", Font.BOLD, Math.max(10, fontSize - 4));
        g2d.setFont(offFont);
        FontMetrics offFm = g2d.getFontMetrics();
        String off = "OFF";
        int offX = x + (badgeSize - offFm.stringWidth(off)) / 2;
        int offY = textY + offFm.getAscent() + 2;
        g2d.drawString(off, offX, offY);
    }

    private void drawPlatformWatermark(Graphics2D g2d, String platform, int width, int height) {
        if (platform == null || platform.isBlank()) return;

        int fontSize = Math.max(10, width / 80);
        Font font = new Font("Arial", Font.ITALIC, fontSize);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        String label = platform.replace("_", " ").toUpperCase();
        int textWidth = fm.stringWidth(label);
        int x = width - textWidth - 10;
        int y = height - 10;

        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.drawString(label, x, y);
    }

    public BufferedImage resize(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
            return resized;
        } finally {
            g2d.dispose();
        }
    }

    private String truncateText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        int available = maxWidth - ellipsisWidth;
        for (int i = text.length(); i > 0; i--) {
            String sub = text.substring(0, i);
            if (fm.stringWidth(sub) <= available) {
                return sub + ellipsis;
            }
        }
        return text.substring(0, 1) + ellipsis;
    }

    public Map<String, String> buildOverlayData(String title, String storeName, BigDecimal currentPrice,
                                                  BigDecimal originalPrice, BigDecimal discountPercent, String platform) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("title", title != null ? title : "");
        data.put("storeName", storeName != null ? storeName : "");
        data.put("currentPrice", currentPrice != null ? formatPrice(currentPrice) : "");
        data.put("originalPrice", originalPrice != null ? formatPrice(originalPrice) : "");
        data.put("discount", discountPercent != null ? discountPercent.setScale(0, RoundingMode.HALF_UP).toString() : "");
        data.put("platform", platform != null ? platform : "");
        return data;
    }

    private String formatPrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) == 0) return "";
        return "R$ " + price.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",");
    }

    private record ColorScheme(Color primary, Color secondary, Color text) {}
}
