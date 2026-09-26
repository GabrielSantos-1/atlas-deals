package com.atlasdeals.search.application.service;

import com.atlasdeals.common.domain.ProductId;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.PutMappingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    private final OpenSearchClient openSearchClient;

    @Value("${opensearch.index-prefix:atlas_deals}")
    private String indexPrefix;

    public IndexingService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @PostConstruct
    public void ensureIndexExists() {
        String indexName = indexPrefix + "_products";
        try {
            boolean exists = openSearchClient.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
            if (!exists) {
                openSearchClient.indices().create(CreateIndexRequest.of(c -> c
                    .index(indexName)
                    .settings(s -> s
                        .numberOfShards("3")
                        .numberOfReplicas("1")
                        .analysis(a -> a
                            .analyzer("brazilian", an -> an.custom(ca -> ca
                                .tokenizer("standard")
                                .filter("lowercase", "asciifolding"))))))
                );
                openSearchClient.indices().putMapping(PutMappingRequest.of(m -> m
                    .index(indexName)
                    .properties("suggest", p -> p
                        .completion(c -> c)))
                );
                log.info("Created index: {}", indexName);
            }
        } catch (IOException e) {
            log.error("Failed to ensure index exists: {}", e.getMessage(), e);
        }
    }

    public void indexProduct(ProductId productId, String title, String description, BigDecimal price,
                              BigDecimal originalPrice, String category, String brand, String storeName,
                              String url, String imageUrl, boolean inStock, String currency) {
        String indexName = indexPrefix + "_products";
        try {
            Map<String, Object> document = Map.of(
                "productId", productId.getValue().toString(),
                "title", title,
                "description", description != null ? description : "",
                "price", price,
                "originalPrice", originalPrice,
                "category", category != null ? category : "",
                "brand", brand != null ? brand : "",
                "storeName", storeName,
                "url", url,
                "imageUrl", imageUrl != null ? imageUrl : "",
                "inStock", inStock,
                "currency", currency,
                "indexedAt", Instant.now().toString()
            );

            openSearchClient.index(IndexRequest.of(i -> i
                .index(indexName)
                .id(productId.getValue().toString())
                .document(document)
                .refresh(Refresh.WaitFor)
            ));
            log.info("Indexed product: {}", productId.getValue());
        } catch (IOException e) {
            log.error("Failed to index product {}: {}", productId.getValue(), e.getMessage(), e);
        }
    }

    public void updateDocument(ProductId productId, Map<String, Object> fields) {
        String indexName = indexPrefix + "_products";
        try {
            openSearchClient.update(up -> up
                .index(indexName)
                .id(productId.getValue().toString())
                .doc(fields),
                Map.class
            );
            log.info("Updated document for product: {}", productId.getValue());
        } catch (IOException e) {
            log.error("Failed to update document {}: {}", productId.getValue(), e.getMessage(), e);
        }
    }

    public void deleteDocument(ProductId productId) {
        String indexName = indexPrefix + "_products";
        try {
            openSearchClient.delete(d -> d
                .index(indexName)
                .id(productId.getValue().toString())
            );
            log.info("Deleted document for product: {}", productId.getValue());
        } catch (IOException e) {
            log.error("Failed to delete document {}: {}", productId.getValue(), e.getMessage(), e);
        }
    }
}
