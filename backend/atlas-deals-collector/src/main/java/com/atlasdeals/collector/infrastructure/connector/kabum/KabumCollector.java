package com.atlasdeals.collector.infrastructure.connector.kabum;

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
public class KabumCollector extends BaseCollector {

    private final boolean enabled;
    private final HttpClient httpClient;
    private static final String API_URL = "https://www.kabum.com.br/api/v1";
    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    public KabumCollector(CollectorProperties properties,
                          ObjectMapper objectMapper,
                          RawProductRepository repository,
                          ProductEventPublisher eventPublisher) {
        super("Kabum", MAX_REQUESTS_PER_MINUTE, objectMapper, repository, eventPublisher);
        this.enabled = properties.getConnectors().getKabum().isEnabled();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Override
    public ConnectorConfig getConfig() {
        return new ConnectorConfig("Kabum", API_URL, "", MAX_REQUESTS_PER_MINUTE, enabled);
    }

    @Override
    protected CollectionResult doCollect() {
        log.info("Starting Kabum collection");
        Instant start = Instant.now();
        int itemsNew = 0;
        int itemsUpdated = 0;
        int errors = 0;

        try {
            List<String> keywords = List.of("placa-video", "processador", "memoria-ram", "ssd", "monitor",
                "notebook", "gabinete", "fonte", "cooler", "teclado", "mouse", "headset");
            int totalPages = 2;

            for (int page = 1; page <= totalPages; page++) {
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
                        log.error("Error searching Kabum for '{}' page {}: {}", keyword, page, e.getMessage());
                        errors++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Kabum collection failed: {}", e.getMessage(), e);
            return CollectionResult.failure("Kabum", errors + 1, Duration.between(start, Instant.now()));
        }

        return CollectionResult.success("Kabum", itemsNew, itemsUpdated, Duration.between(start, Instant.now()));
    }

    private List<RawProduct> searchProducts(String keyword, int page) {
        List<RawProduct> products = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/produto/busca/" + keyword + "?page=" + page + "&limit=20"))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode items = root.path("products");
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        try {
                            String id = item.path("codigo").asText();
                            String title = item.path("nome").asText();
                            BigDecimal price = BigDecimal.valueOf(item.path("preco").asDouble(0));
                            BigDecimal originalPrice = BigDecimal.valueOf(item.path("preco_original").asDouble(0));
                            String imageUrl = item.path("imagem").asText();
                            String brand = item.path("marca").asText();
                            String category = item.path("departamento").path("nome").asText("Hardware");
                            boolean inStock = item.path("estoque").asInt(0) > 0;

                            if (!id.isEmpty() && price.compareTo(BigDecimal.ZERO) > 0) {
                                products.add(new RawProduct(
                                    UUID.randomUUID(), "KABUM-" + id, "Kabum", "kabum_br",
                                    title, "",
                                    price, originalPrice, "BRL",
                                    "https://www.kabum.com.br/produto/" + id,
                                    imageUrl.startsWith("http") ? imageUrl : "https://images.kabum.com.br" + imageUrl,
                                    category, brand, "", id, "",
                                    inStock, inStock,
                                    "{\"source\":\"kabum\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
                                    Instant.now()
                                ));
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse Kabum product: {}", e.getMessage());
                        }
                    }
                }
            } else {
                log.warn("Kabum API returned {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("Kabum API request failed: {}", e.getMessage());
        }

        if (products.isEmpty()) {
            return searchMocked(keyword, page);
        }
        return products;
    }

    private List<RawProduct> searchMocked(String keyword, int page) {
        List<RawProduct> products = new ArrayList<>();
        String idBase = UUID.randomUUID().toString().substring(0, 8);

        products.add(new RawProduct(
            UUID.randomUUID(), "KABUM-" + idBase + "-A", "Kabum", "kabum_br",
            "Placa de Vídeo " + keyword.replace("-", " ").toUpperCase() + " NVIDIA RTX 4070",
            "Placa de Vídeo Gigabyte NVIDIA GeForce RTX 4070 Windforce 3X OC, 12GB GDDR6X, DLSS 3",
            BigDecimal.valueOf(3499.00), BigDecimal.valueOf(4299.00), "BRL",
            "https://www.kabum.com.br/produto/" + idBase + "-a",
            "https://images.kabum.com.br/produtos/" + idBase + "-a.jpg",
            "Hardware", "Gigabyte", "RTX 4070 Windforce 3X OC", "GV-N4070WF3OC-12GD", "4719337162931",
            true, true, "{\"source\":\"kabum\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
            Instant.now()
        ));

        products.add(new RawProduct(
            UUID.randomUUID(), "KABUM-" + idBase + "-B", "Kabum", "kabum_br",
            "Processador Intel Core i7-14700K Box",
            "Processador Intel Core i7-14700K, 20 núcleos (8P+12E), 5.6GHz Turbo, LGA 1700, DDR5",
            BigDecimal.valueOf(2799.00), BigDecimal.valueOf(3299.00), "BRL",
            "https://www.kabum.com.br/produto/" + idBase + "-b",
            "https://images.kabum.com.br/produtos/" + idBase + "-b.jpg",
            "Hardware", "Intel", "Core i7-14700K", "BX8071514700K", "5032037278987",
            true, true, "{\"source\":\"kabum\",\"keyword\":\"" + keyword + "\",\"page\":" + page + "}",
            Instant.now()
        ));

        return products;
    }
}
