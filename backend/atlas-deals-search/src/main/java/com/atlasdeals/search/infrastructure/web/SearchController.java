package com.atlasdeals.search.infrastructure.web;

import com.atlasdeals.search.application.service.IndexingService;
import com.atlasdeals.search.application.service.ProductSearchService;
import com.atlasdeals.search.application.service.ProductSearchService.SearchResult;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final ProductSearchService searchService;
    private final IndexingService indexingService;

    public SearchController(ProductSearchService searchService, IndexingService indexingService) {
        this.searchService = searchService;
        this.indexingService = indexingService;
    }

    @GetMapping
    public ResponseEntity<SearchResult> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String store,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        SearchResult result = searchService.search(q, category, minPrice, maxPrice, store, sort, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<String>> suggest(@RequestParam String q) {
        return ResponseEntity.ok(searchService.suggest(q));
    }

    @PostMapping("/index")
    public ResponseEntity<Void> triggerReindex() {
        indexingService.ensureIndexExists();
        return ResponseEntity.ok().build();
    }
}
