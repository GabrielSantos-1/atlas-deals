package com.atlasdeals.search.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.RangeQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.CompletionSuggest;
import org.opensearch.client.opensearch.core.search.CompletionSuggester;
import org.opensearch.client.opensearch.core.search.FieldSuggester;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.Suggest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProductSearchService {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchService.class);

    private final OpenSearchClient openSearchClient;
    private final ObjectMapper objectMapper;

    @Value("${opensearch.index-prefix:atlas_deals}")
    private String indexPrefix;

    public ProductSearchService(OpenSearchClient openSearchClient, ObjectMapper objectMapper) {
        this.openSearchClient = openSearchClient;
        this.objectMapper = objectMapper;
    }

    public SearchResult search(String query, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                String store, String sort, int page, int size) {
        String indexName = indexPrefix + "_products";
        try {
            List<Query> mustQueries = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                mustQueries.add(MatchQuery.of(m -> m.field("title").query(query))._toQuery());
            }
            if (category != null && !category.isBlank()) {
                mustQueries.add(TermQuery.of(t -> t.field("category").value(category))._toQuery());
            }
            if (store != null && !store.isBlank()) {
                mustQueries.add(TermQuery.of(t -> t.field("storeName").value(store))._toQuery());
            }
            if (minPrice != null || maxPrice != null) {
                var range = RangeQuery.of(r -> {
                    r.field("price");
                    if (minPrice != null) r.gte(minPrice.doubleValue());
                    if (maxPrice != null) r.lte(maxPrice.doubleValue());
                    return r;
                });
                mustQueries.add(range._toQuery());
            }

            Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

            var sortBuilder = new ArrayList<Map<String, SortOrder>>();
            if ("price_asc".equals(sort)) {
                sortBuilder.add(Map.of("price", SortOrder.Asc));
            } else if ("price_desc".equals(sort)) {
                sortBuilder.add(Map.of("price", SortOrder.Desc));
            } else if ("newest".equals(sort)) {
                sortBuilder.add(Map.of("indexedAt", SortOrder.Desc));
            } else {
                sortBuilder.add(Map.of("_score", SortOrder.Desc));
            }

            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(indexName)
                .query(boolQuery)
                .from(page * size)
                .size(size)
                .sort(sortBuilder)
                .aggregations("by_category", Aggregation.of(a -> a.terms(t -> t.field("category"))))
                .aggregations("by_store", Aggregation.of(a -> a.terms(t -> t.field("storeName"))))
                .aggregations("avg_price", Aggregation.of(a -> a.avg(t -> t.field("price"))))
            );

            SearchResponse<JsonNode> response = openSearchClient.search(searchRequest, JsonNode.class);

            List<Map<String, Object>> results = new ArrayList<>();
            for (Hit<JsonNode> hit : response.hits().hits()) {
                Map<String, Object> doc = objectMapper.convertValue(hit.source(), Map.class);
                doc.put("_score", hit.score());
                results.add(doc);
            }

            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            int totalPages = (int) Math.ceil((double) total / size);

            return new SearchResult(results, total, page, totalPages);
        } catch (IOException e) {
            log.error("Search failed: {}", e.getMessage(), e);
            return new SearchResult(Collections.emptyList(), 0, page, 0);
        }
    }

    public List<String> suggest(String prefix) {
        String indexName = indexPrefix + "_products";
        try {
            var response = openSearchClient.search(SearchRequest.of(s -> s
                .index(indexName)
                .suggest(Map.of("title_suggest", FieldSuggester.of(f -> f
                    .text(prefix)
                    .completion(CompletionSuggester.of(c -> c.field("suggest"))))))
            ), JsonNode.class);

            List<String> suggestions = new ArrayList<>();
            Map<String, List<Suggest<JsonNode>>> suggestMap = response.suggest();
            if (suggestMap != null && suggestMap.containsKey("title_suggest")) {
                for (Suggest<JsonNode> suggest : suggestMap.get("title_suggest")) {
                    for (CompletionSuggest<JsonNode> option : suggest.completion().options()) {
                        suggestions.add(option.text());
                    }
                }
            }
            return suggestions;
        } catch (IOException e) {
            log.error("Suggest failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public record SearchResult(List<Map<String, Object>> results, long total, int page, int totalPages) {}
}
