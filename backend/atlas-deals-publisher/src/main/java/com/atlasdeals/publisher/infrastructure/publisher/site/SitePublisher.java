package com.atlasdeals.publisher.infrastructure.publisher.site;

import com.atlasdeals.publisher.application.dto.PublishRequest;
import com.atlasdeals.publisher.application.dto.PublishResult;
import com.atlasdeals.publisher.infrastructure.config.PublisherConfig;
import com.atlasdeals.publisher.infrastructure.publisher.BasePublisher;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class SitePublisher extends BasePublisher {

    private static final Logger log = LoggerFactory.getLogger(SitePublisher.class);
    private static final int MAX_REQUESTS_PER_SECOND = 100;

    private final String baseUrl;
    private final Path outputDir;

    public SitePublisher(PublisherConfig.PlatformConfig config,
                         MeterRegistry meterRegistry,
                         int maxRetries,
                         long retryBackoffMs) {
        super("site", meterRegistry, MAX_REQUESTS_PER_SECOND, maxRetries, retryBackoffMs);
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : "https://atlasdeals.com.br";

        String outputDirStr = config.getOutputDir();
        if (outputDirStr == null || outputDirStr.isBlank()) {
            outputDirStr = "./data/site/pages";
        }
        this.outputDir = Paths.get(outputDirStr);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected PublishResult doPublish(PublishRequest request) {
        try {
            Files.createDirectories(outputDir);

            String slug = generateSlug(request);
            String pageId = UUID.randomUUID().toString().substring(0, 8);
            String fileName = slug + "-" + pageId + ".html";
            Path filePath = outputDir.resolve(fileName);

            String htmlContent = generateHtmlPage(request, slug);

            Files.writeString(filePath, htmlContent, StandardCharsets.UTF_8);

            String pageUrl = baseUrl + "/promocoes/" + slug;

            Map<String, Object> siteMetadata = new LinkedHashMap<>();
            siteMetadata.put("fileName", fileName);
            siteMetadata.put("filePath", filePath.toAbsolutePath().toString());
            siteMetadata.put("pageUrl", pageUrl);
            siteMetadata.put("fileSize", htmlContent.length());

            if (request.metadata() != null) {
                siteMetadata.putAll(request.metadata());
            }

            log.info("Published site page: {} ({} bytes)", pageUrl, htmlContent.length());

            return new PublishResult(true, slug, null, Instant.now());
        } catch (IOException e) {
            log.error("Failed to write site page: {}", e.getMessage(), e);
            return new PublishResult(false, null, "Failed to write page: " + e.getMessage(), Instant.now());
        }
    }

    private String generateSlug(PublishRequest request) {
        String productId = request.productId();
        if (productId != null) {
            return "deal-" + productId.replace("-", "").substring(0, Math.min(8, productId.length()));
        }
        return "deal-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateHtmlPage(PublishRequest request, String slug) {
        String content = request.content() != null ? request.content() : "";
        String imageUrl = request.imageUrl();
        String affiliateUrl = request.affiliateUrl();

        String safeContent = content
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("\n", "<br>");

        String imageTag = imageUrl != null && !imageUrl.isBlank()
            ? "<img src=\"" + escapeHtml(imageUrl) + "\" alt=\"Promoção\" style=\"max-width:100%%;height:auto;border-radius:8px;margin:20px 0;\">"
            : "";

        String affiliateTag = affiliateUrl != null && !affiliateUrl.isBlank()
            ? "<a href=\"" + escapeHtml(affiliateUrl) + "\" target=\"_blank\" rel=\"nofollow noopener\" class=\"cta-button\">Ver Oferta</a>"
            : "";

        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>%s</title>
    <meta name="description" content="%s">
    <script type="application/ld+json">
    {
        "@context": "https://schema.org",
        "@type": "Product",
        "name": "%s",
        "description": "%s",
        "url": "%s/promocoes/%s"
    }
    </script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #333; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; padding: 20px; }
        .card { background: #fff; border-radius: 12px; padding: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
        h1 { font-size: 1.8rem; color: #1a1a2e; margin-bottom: 20px; }
        .cta-button { display: inline-block; padding: 14px 32px; background: #e94560; color: #fff; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 1.1rem; margin: 20px 0; transition: background 0.2s; }
        .cta-button:hover { background: #d63851; }
        .content { font-size: 1.05rem; color: #444; }
        .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; font-size: 0.85rem; color: #888; }
    </style>
</head>
<body>
    <div class="container">
        <div class="card">
            <h1>%s</h1>
            %s
            <div class="content">%s</div>
            %s
            <div class="footer">
                <p>Atlas Deals - Sua central de promoções</p>
                <p><a href="%s">atlasdeals.com.br</a></p>
            </div>
        </div>
    </div>
</body>
</html>""".formatted(
                escapeHtml(slug),
                escapeHtml(truncate(safeContent.replace("<br>", " "), 160)),
                escapeHtml(slug),
                escapeHtml(truncate(safeContent.replace("<br>", " "), 300)),
                baseUrl,
                escapeHtml(slug),
                escapeHtml(slug),
                imageTag,
                safeContent,
                affiliateTag,
                baseUrl
            );
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private String truncate(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) return input;
        return input.substring(0, maxLength - 3) + "...";
    }
}
