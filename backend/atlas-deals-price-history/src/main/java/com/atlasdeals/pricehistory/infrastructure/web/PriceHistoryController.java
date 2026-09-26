package com.atlasdeals.pricehistory.infrastructure.web;

import com.atlasdeals.pricehistory.application.dto.PriceComparison;
import com.atlasdeals.pricehistory.application.dto.PriceStats;
import com.atlasdeals.pricehistory.application.service.PriceHistoryService;
import com.atlasdeals.pricehistory.domain.model.PriceRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/price-history")
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    public PriceHistoryController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<PriceRecord>> getPriceHistory(
            @PathVariable UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.INSTANT) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.INSTANT) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<PriceRecord> history = priceHistoryService.getPriceHistory(productId, from, to, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{productId}/latest")
    public ResponseEntity<PriceRecord> getLatestPrice(@PathVariable UUID productId) {
        return priceHistoryService.getLatestPrice(productId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{productId}/lowest")
    public ResponseEntity<java.math.BigDecimal> getLowestPrice(@PathVariable UUID productId) {
        return priceHistoryService.getLowestPrice(productId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{productId}/stats")
    public ResponseEntity<PriceStats> getPriceStats(
            @PathVariable UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.INSTANT) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.INSTANT) Instant to) {
        PriceStats stats = priceHistoryService.getPriceStats(productId, from, to);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/compare")
    public ResponseEntity<PriceComparison> comparePrices(
            @RequestParam UUID productId1,
            @RequestParam UUID productId2) {
        PriceComparison comparison = priceHistoryService.comparePrices(productId1, productId2);
        return ResponseEntity.ok(comparison);
    }
}
