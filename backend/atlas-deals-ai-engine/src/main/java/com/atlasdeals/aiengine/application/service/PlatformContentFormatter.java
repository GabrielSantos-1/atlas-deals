package com.atlasdeals.aiengine.application.service;

import com.atlasdeals.aiengine.domain.model.AiContent;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PlatformContentFormatter {

    public Map<String, String> formatForAllPlatforms(AiContent content) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("telegram", formatTelegram(content));
        result.put("whatsapp", formatWhatsApp(content));
        result.put("instagram", formatInstagram(content));
        result.put("facebook", formatFacebook(content));
        result.put("threads", formatThreads(content));
        result.put("twitter", formatTwitter(content));
        result.put("discord", formatDiscord(content));
        result.put("site", formatSite(content));
        result.put("newsletter", formatNewsletter(content));
        result.put("push", formatPush(content));
        return result;
    }

    public String formatForPlatform(AiContent content, String platform) {
        return switch (platform.toLowerCase()) {
            case "telegram" -> formatTelegram(content);
            case "whatsapp" -> formatWhatsApp(content);
            case "instagram" -> formatInstagram(content);
            case "facebook" -> formatFacebook(content);
            case "threads" -> formatThreads(content);
            case "twitter", "x" -> formatTwitter(content);
            case "discord" -> formatDiscord(content);
            case "site" -> formatSite(content);
            case "newsletter" -> formatNewsletter(content);
            case "push" -> formatPush(content);
            default -> content.getSummary();
        };
    }

    private String formatTelegram(AiContent content) {
        String level = getLevelEmoji(content.getOpportunityLevel());
        String text = String.format("""
            %s *%s* %s

            %s

            💰 *Preco:* Consulte a loja
            🏷️ *Desconto:* %s
            📊 *Nivel:* %s

            %s

            %s
            """,
            level, content.getTitle(), level,
            content.getDescription() != null ? truncate(content.getDescription(), 800) : "",
            content.getOpportunityLevel() != null ? content.getOpportunityLevel() : "Consulte",
            content.getOpportunityLevel() != null ? getLevelLabel(content.getOpportunityLevel()) : "",
            content.getHashtags() != null ? content.getHashtags() : "",
            content.getSummary() != null ? "*" + content.getSummary() + "*" : ""
        );
        return truncate(text, 4096);
    }

    private String formatWhatsApp(AiContent content) {
        String text = String.format("""
            🔥 *%s*

            %s

            💰 *Oferta imperdivel!*
            📌 %s

            %s
            """,
            content.getTitle(),
            content.getSummary() != null ? content.getSummary() : "",
            content.getOpportunityLevel() != null ? "Nivel: " + getLevelLabel(content.getOpportunityLevel()) : "",
            content.getHashtags() != null ? content.getHashtags() : ""
        );
        return truncate(text, 1024);
    }

    private String formatInstagram(AiContent content) {
        String text = String.format("""
            🔥 %s

            %s

            %s

            🏷️ %s

            🛒 Link na bio!
            %s
            """,
            content.getTitle(),
            content.getSummary() != null ? content.getSummary() : "",
            content.getDescription() != null ? truncate(content.getDescription(), 500) : "",
            content.getOpportunityLevel() != null ? getLevelLabel(content.getOpportunityLevel()) : "Oferta especial",
            content.getHashtags() != null ? content.getHashtags() : ""
        );
        return truncate(text, 2200);
    }

    private String formatFacebook(AiContent content) {
        String text = String.format("""
            🛒 *%s*

            %s

            %s

            %s
            👉 Clique para mais detalhes e garantir o seu!
            """,
            content.getTitle(),
            content.getDescription() != null ? truncate(content.getDescription(), 1000) : "",
            content.getTechnicalSummary() != null ? "📋 " + content.getTechnicalSummary() : "",
            content.getHashtags() != null ? content.getHashtags() : ""
        );
        return truncate(text, 5000);
    }

    private String formatThreads(AiContent content) {
        String text = String.format("""
            %s

            %s

            %s
            """,
            content.getTitle(),
            content.getSummary() != null ? content.getSummary() : "",
            content.getHashtags() != null ? content.getHashtags() : ""
        );
        return truncate(text, 500);
    }

    private String formatTwitter(AiContent content) {
        String text = String.format("""
            🔥 %s

            %s

            %s
            """,
            content.getTitle(),
            content.getSummary() != null ? content.getSummary() : "",
            content.getHashtags() != null ? truncateHashtagsForTwitter(content.getHashtags()) : ""
        );
        return truncate(text, 280);
    }

    private String formatDiscord(AiContent content) {
        String pros = content.getPros() != null ? content.getPros() : "[]";
        String cons = content.getCons() != null ? content.getCons() : "[]";
        String text = String.format("""
            **🔥 %s**

            📝 %s

            **✅ Pros:**
            %s

            **❌ Contras:**
            %s

            %s
            """,
            content.getTitle(),
            content.getDescription() != null ? truncate(content.getDescription(), 500) : "",
            formatBulletList(pros),
            formatBulletList(cons),
            content.getHashtags() != null ? content.getHashtags() : ""
        );
        return truncate(text, 4000);
    }

    private String formatSite(AiContent content) {
        String pros = content.getPros() != null ? content.getPros() : "[]";
        String cons = content.getCons() != null ? content.getCons() : "[]";
        String techSummary = content.getTechnicalSummary() != null ? content.getTechnicalSummary() : "";
        String profile = content.getRecommendedProfile() != null ? content.getRecommendedProfile() : "";

        return String.format("""
            <article class="product-content">
                <h1>%s</h1>
                <p class="summary">%s</p>
                <div class="description">
                    %s
                </div>
                <div class="tech-summary">
                    <h2>Resumo Técnico</h2>
                    %s
                </div>
                <div class="pros-cons">
                    <div class="pros">
                        <h3>✅ Pros</h3>
                        %s
                    </div>
                    <div class="cons">
                        <h3>❌ Contras</h3>
                        %s
                    </div>
                </div>
                <p class="profile"><strong>Perfil Recomendado:</strong> %s</p>
                <p class="opportunity"><strong>Nivel de Oportunidade:</strong> %s</p>
                <p class="tags">%s</p>
            </article>
            """,
            escapeHtml(content.getTitle()),
            escapeHtml(content.getSummary() != null ? content.getSummary() : ""),
            escapeHtml(content.getDescription() != null ? content.getDescription() : ""),
            escapeHtml(techSummary),
            formatHtmlList(pros),
            formatHtmlList(cons),
            escapeHtml(profile),
            content.getOpportunityLevel() != null ? content.getOpportunityLevel() : "",
            content.getHashtags() != null ? content.getHashtags() : ""
        );
    }

    private String formatNewsletter(AiContent content) {
        String pros = content.getPros() != null ? content.getPros() : "[]";
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #1a1a2e; color: white; padding: 20px; text-align: center;">
                    <h1>🔥 %s</h1>
                </div>
                <div style="padding: 20px;">
                    <p style="font-size: 16px; line-height: 1.5;">%s</p>
                    <p style="font-size: 14px; color: #666;">%s</p>
                    <div style="background: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3>✅ Pros</h3>
                        %s
                    </div>
                    <p style="text-align: center; margin-top: 30px;">
                        <a href="#" style="background: #e94560; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px;">
                            Ver Oferta
                        </a>
                    </p>
                    <p style="font-size: 12px; color: #999; text-align: center; margin-top: 20px;">
                        %s
                    </p>
                </div>
            </body>
            </html>
            """,
            escapeHtml(content.getTitle()),
            escapeHtml(content.getDescription() != null ? content.getDescription() : ""),
            escapeHtml(content.getSummary() != null ? content.getSummary() : ""),
            formatHtmlList(pros),
            content.getHashtags() != null ? content.getHashtags() : ""
        );
    }

    private String formatPush(AiContent content) {
        String text = String.format("%s - %s",
            content.getTitle(),
            content.getOpportunityLevel() != null ? getLevelLabel(content.getOpportunityLevel()) : "Oferta"
        );
        return truncate(text, 100);
    }

    private String getLevelEmoji(String level) {
        if (level == null) return "🟡";
        return switch (level) {
            case "HIGH" -> "🔴";
            case "MEDIUM" -> "🟡";
            case "LOW" -> "🟢";
            default -> "🟡";
        };
    }

    private String getLevelLabel(String level) {
        if (level == null) return "";
        return switch (level) {
            case "HIGH" -> "🔥 Oportunidade Unica!";
            case "MEDIUM" -> "⚡ Boa Oferta";
            case "LOW" -> "💡 Oferta Regular";
            default -> "";
        };
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private String truncateHashtagsForTwitter(String hashtags) {
        if (hashtags == null) return "";
        String[] tags = hashtags.split(",");
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            String t = tag.trim();
            if (sb.length() + t.length() + 1 > 100) break;
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(t);
        }
        return sb.toString();
    }

    private String formatBulletList(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank() || jsonArray.equals("[]")) return "Nenhum";
        String cleaned = jsonArray.replaceAll("[\\[\\]\"]", "");
        String[] items = cleaned.split(",");
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append("- ").append(item.trim()).append("\n");
        }
        return sb.toString();
    }

    private String formatHtmlList(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank() || jsonArray.equals("[]")) return "<p>Nenhum</p>";
        String cleaned = jsonArray.replaceAll("[\\[\\]\"]", "");
        String[] items = cleaned.split(",");
        StringBuilder sb = new StringBuilder("<ul>");
        for (String item : items) {
            sb.append("<li>").append(escapeHtml(item.trim())).append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
