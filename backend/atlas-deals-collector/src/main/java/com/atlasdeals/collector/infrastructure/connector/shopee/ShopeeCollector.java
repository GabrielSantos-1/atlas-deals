package com.atlasdeals.collector.infrastructure.connector.shopee;

import com.atlasdeals.collector.application.dto.CollectionResult;
import com.atlasdeals.collector.application.dto.ConnectorConfig;
import com.atlasdeals.collector.domain.model.RawProduct;
import com.atlasdeals.collector.domain.repository.RawProductRepository;
import com.atlasdeals.collector.infrastructure.config.CollectorProperties;
import com.atlasdeals.collector.infrastructure.connector.BaseCollector;
import com.atlasdeals.collector.infrastructure.messaging.ProductEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ShopeeCollector extends BaseCollector {

    private final String partnerId;
    private final String affiliateKey;
    private final boolean enabled;
    private final HttpClient httpClient;
    private static final String API_URL = "https://affiliate.shopee.com.br/api/v2";
    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    public ShopeeCollector(CollectorProperties properties,
                           ObjectMapper objectMapper,
                           RawProductRepository repository,
                           ProductEventPublisher eventPublisher) {
        super("Shopee", MAX_REQUESTS_PER_MINUTE, objectMapper, repository, eventPublisher);
        this.partnerId = properties.getConnectors().getShopee().getPartnerId();
        this.affiliateKey = properties.getConnectors().getShopee().getAffiliateKey();
        this.enabled = properties.getConnectors().getShopee().isEnabled();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public ConnectorConfig getConfig() {
        return new ConnectorConfig("Shopee", API_URL, affiliateKey, MAX_REQUESTS_PER_MINUTE, enabled);
    }

    @Override
    protected CollectionResult doCollect() {
        log.info("Starting Shopee collection (partnerId={})", partnerId.isEmpty() ? "not-configured" : "configured");
        Instant start = Instant.now();
        int itemsNew = 0;
        int itemsUpdated = 0;
        int errors = 0;

        try {
            List<String> keywords = List.of("smartphone", "notebook", "fone", "tv", "tablet",
                "monitor", "teclado", "mouse", "ssd", "gpu", "cpu", "ram");
            int totalPages = 2;

            for (int page = 0; page < totalPages; page++) {
                for (String keyword : keywords) {
                    try {
                        List<RawProduct> products = searchProducts(keyword, page);
                        for (RawProduct product : products) {
                            if (isDuplicate(product)) {
                                itemsUpdated++;
                            } else {
                                saveProduct(product);
                                itemsNew++;
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error searching Shopee for '{}' page {}: {}", keyword, page, e.getMessage());
                        errors++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Shopee collection failed: {}", e.getMessage(), e);
            return CollectionResult.failure("Shopee", errors + 1, Duration.between(start, Instant.now()));
        }

        return CollectionResult.success("Shopee", itemsNew, itemsUpdated, Duration.between(start, Instant.now()));
    }

    private List<RawProduct> searchProducts(String keyword, int page) {
        if (!partnerId.isEmpty() && !affiliateKey.isEmpty()) {
            return searchViaApi(keyword, page);
        }
        return searchMocked(keyword, page);
    }

    private List<RawProduct> searchViaApi(String keyword, int page) {
        List<RawProduct> products = new ArrayList<>();
        try {
            String json = """
                {"partner_id":%s,"aff_click_key":"%s","keyword":"%s","page":%d,"limit":20}
                """.formatted(partnerId, affiliateKey, keyword, page + 1);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/product/search"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .timeout(Duration.ofSeconds(15))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode items = root.path("data").path("products");
                for (JsonNode item : items) {
                    try {
                        RawProduct product = new RawProduct(
                            UUID.randomUUID(),
                            "SHOP-" + item.path("product_id").asText(),
                            "Shopee", "shopee_br",
                            item.path("product_name").asText(),
                            item.path("description").asText(""),
                            BigDecimal.valueOf(item.path("price").asDouble()),
                            BigDecimal.valueOf(item.path("price_before_discount").asDouble(0)),
                            "BRL",
                            "https://shopee.com.br/product/" + item.path("product_id").asText(),
                            item.path("image_url").asText(),
                            item.path("category_name").asText("Eletrônicos"),
                            item.path("brand").asText(""),
                            item.path("model").asText(""),
                            item.path("sku").asText(""),
                            "",
                            item.path("stock").asInt() > 0,
                            item.path("stock").asInt() > 0,
                            "{\"source\":\"shopee\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
                            Instant.now()
                        );
                        if (product.getTitle() != null && !product.getTitle().isBlank()) {
                            products.add(product);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse Shopee product: {}", e.getMessage());
                    }
                }
            } else {
                log.warn("Shopee API returned {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Shopee API request failed: {}", e.getMessage());
        }
        return products;
    }

    private List<RawProduct> searchMocked(String keyword, int page) {
        List<RawProduct> products = new ArrayList<>();
        String baseId = UUID.randomUUID().toString().substring(0, 8);

        products.add(new RawProduct(
            UUID.randomUUID(), "SHOP-" + baseId + "-1", "Shopee", "shopee_br",
            keyword.substring(0, 1).toUpperCase() + keyword.substring(1) + " Xiaomi Mi 13 Ultra 512GB",
            "Xiaomi Mi 13 Ultra 512GB 12GB RAM - Tela 6.73\" - Câmera 50MP - Preto",
            BigDecimal.valueOf(4999.00), BigDecimal.valueOf(6999.00), "BRL",
            "https://shopee.com.br/product/" + baseId + "-1",
            "https://cf.shopee.com.br/file/" + baseId + "-1",
            "Eletrônicos", "Xiaomi", "Mi 13 Ultra", "XMS13U-512", "6934177740011",
            true, true, "{\"source\":\"shopee\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
            Instant.now()
        ));

        products.add(new RawProduct(
            UUID.randomUUID(), "SHOP-" + baseId + "-2", "Shopee", "shopee_br",
            "Smart TV " + keyword.substring(0, 1).toUpperCase() + keyword.substring(1) + " LG 55\" 4K OLED",
            "LG OLED55C3 55\" 4K Smart TV - OLED evo - Dolby Vision IQ - IA",
            BigDecimal.valueOf(3999.00), BigDecimal.valueOf(5499.00), "BRL",
            "https://shopee.com.br/product/" + baseId + "-2",
            "https://cf.shopee.com.br/file/" + baseId + "-2",
            "Eletrônicos", "LG", "OLED55C3", "LG-OLED55C3", "8806084076341",
            true, true, "{\"source\":\"shopee\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
            Instant.now()
        ));

        return products;
    }
}
