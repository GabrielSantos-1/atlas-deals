package com.atlasdeals.analytics.infrastructure.web;

import com.atlasdeals.analytics.application.dto.ConversionFunnel;
import com.atlasdeals.analytics.application.dto.DashboardMetrics;
import com.atlasdeals.analytics.application.dto.RealTimeMetrics;
import com.atlasdeals.analytics.application.dto.RevenueBreakdown;
import com.atlasdeals.analytics.application.dto.TopProduct;
import com.atlasdeals.analytics.application.service.AnalyticsService;
import com.atlasdeals.analytics.infrastructure.cache.AnalyticsCacheService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsCacheService cacheService;

    public AnalyticsController(AnalyticsService analyticsService, AnalyticsCacheService cacheService) {
        this.analyticsService = analyticsService;
        this.cacheService = cacheService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardMetrics> getDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String cacheKey = "dashboard:" + from + ":" + to;
        DashboardMetrics cached = cacheService.getDashboardMetrics(cacheKey);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        DashboardMetrics metrics = analyticsService.getDashboardMetrics(from, to);
        cacheService.cacheDashboardMetrics(cacheKey, metrics);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/revenue/platform")
    public ResponseEntity<RevenueBreakdown> getRevenueByPlatform(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getRevenueByPlatform(from, to));
    }

    @GetMapping("/revenue/category")
    public ResponseEntity<RevenueBreakdown> getRevenueByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getRevenueByCategory(from, to));
    }

    @GetMapping("/revenue/store")
    public ResponseEntity<RevenueBreakdown> getRevenueByStore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getRevenueByStore(from, to));
    }

    @GetMapping("/products/top")
    public ResponseEntity<List<TopProduct>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getTopProducts(limit, from, to));
    }

    @GetMapping("/funnel")
    public ResponseEntity<ConversionFunnel> getConversionFunnel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.getConversionFunnel(from, to));
    }

    @GetMapping("/realtime")
    public ResponseEntity<RealTimeMetrics> getRealTimeMetrics() {
        return ResponseEntity.ok(analyticsService.getRealTimeMetrics());
    }
}
