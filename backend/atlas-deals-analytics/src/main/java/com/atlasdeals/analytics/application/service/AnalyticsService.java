package com.atlasdeals.analytics.application.service;

import com.atlasdeals.analytics.application.dto.ConversionFunnel;
import com.atlasdeals.analytics.application.dto.DashboardMetrics;
import com.atlasdeals.analytics.application.dto.RealTimeMetrics;
import com.atlasdeals.analytics.application.dto.RevenueBreakdown;
import com.atlasdeals.analytics.application.dto.TopProduct;
import com.atlasdeals.analytics.domain.model.AnalyticsEvent;
import com.atlasdeals.analytics.domain.model.DailyMetric;
import com.atlasdeals.analytics.domain.repository.AnalyticsEventRepository;
import com.atlasdeals.analytics.domain.repository.DailyMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsEventRepository eventRepository;
    private final DailyMetricRepository dailyMetricRepository;

    public AnalyticsService(AnalyticsEventRepository eventRepository,
                            DailyMetricRepository dailyMetricRepository) {
        this.eventRepository = eventRepository;
        this.dailyMetricRepository = dailyMetricRepository;
    }

    @Transactional
    public AnalyticsEvent recordEvent(AnalyticsEvent event) {
        event.setProcessedAt(Instant.now());
        AnalyticsEvent saved = eventRepository.save(event);
        updateDailyMetrics(event);
        log.info("Recorded analytics event {} for product {}", event.getEventType(), event.getProductId());
        return saved;
    }

    private void updateDailyMetrics(AnalyticsEvent event) {
        LocalDate eventDate = event.getEventTime().atZone(ZoneOffset.UTC).toLocalDate();
        String platform = event.getPlatform();

        Optional<DailyMetric> existing = dailyMetricRepository.findByDateAndPlatform(eventDate, platform);
        DailyMetric metric;
        if (existing.isPresent()) {
            metric = existing.get();
        } else {
            metric = new DailyMetric();
            metric.setDate(eventDate);
            metric.setPlatform(platform);
            metric.setCategory(event.getCategory());
            metric.setStoreName(event.getStoreName());
            metric.setTotalRevenue(BigDecimal.ZERO);
            metric.setRevenuePerClick(BigDecimal.ZERO);
        }

        switch (event.getEventType()) {
            case "CLICK" -> metric.setTotalClicks(metric.getTotalClicks() + 1);
            case "CONVERSION" -> {
                metric.setTotalConversions(metric.getTotalConversions() + 1);
                if (event.getRevenue() != null) {
                    metric.setTotalRevenue(metric.getTotalRevenue().add(event.getRevenue()));
                }
            }
            case "VIEW" -> metric.setTotalViews(metric.getTotalViews() + 1);
            case "SHARE" -> metric.setTotalShares(metric.getTotalShares() + 1);
        }

        long clicks = metric.getTotalClicks();
        long views = metric.getTotalViews();
        long conversions = metric.getTotalConversions();
        metric.setCtr(calculateCTR(views, clicks));
        metric.setConversionRate(calculateConversionRate(clicks, conversions));
        if (clicks > 0 && metric.getTotalRevenue() != null) {
            metric.setRevenuePerClick(metric.getTotalRevenue().divide(BigDecimal.valueOf(clicks), 4, RoundingMode.HALF_UP));
        }

        long uniqueProducts = eventRepository.countDistinctProductIdsBetween(
            eventDate.atStartOfDay(ZoneOffset.UTC).toInstant(),
            eventDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());
        metric.setUniqueProducts(uniqueProducts);

        dailyMetricRepository.save(metric);
    }

    public DashboardMetrics getDashboardMetrics(LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        BigDecimal totalRevenue = eventRepository.sumRevenueBetween(fromInstant, toInstant);
        long totalClicks = eventRepository.countByEventTypeAndTimeBetween("CLICK", fromInstant, toInstant);
        long totalConversions = eventRepository.countByEventTypeAndTimeBetween("CONVERSION", fromInstant, toInstant);
        long totalViews = eventRepository.countByEventTypeAndTimeBetween("VIEW", fromInstant, toInstant);

        double avgCTR = calculateCTR(totalViews, totalClicks);
        double avgConversionRate = calculateConversionRate(totalClicks, totalConversions);

        Duration periodDuration = Duration.between(fromInstant, toInstant);
        Instant prevFrom = fromInstant.minus(periodDuration);
        Instant prevTo = fromInstant;

        BigDecimal prevRevenue = eventRepository.sumRevenueBetween(prevFrom, prevTo);
        long prevClicks = eventRepository.countByEventTypeAndTimeBetween("CLICK", prevFrom, prevTo);

        double revenueChange = calculatePercentageChange(prevRevenue, totalRevenue);
        double clickChange = calculatePercentageChange(BigDecimal.valueOf(prevClicks), BigDecimal.valueOf(totalClicks));

        return new DashboardMetrics(totalRevenue, totalClicks, totalConversions, totalViews,
            avgCTR, avgConversionRate, revenueChange, clickChange);
    }

    public RevenueBreakdown getRevenueByPlatform(LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> results = eventRepository.sumRevenueGroupByPlatform(fromInstant, toInstant);
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        for (Object[] row : results) {
            breakdown.put((String) row[0], (BigDecimal) row[1]);
        }
        return new RevenueBreakdown(breakdown);
    }

    public RevenueBreakdown getRevenueByCategory(LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> results = eventRepository.sumRevenueGroupByCategory(fromInstant, toInstant);
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        for (Object[] row : results) {
            breakdown.put((String) row[0], (BigDecimal) row[1]);
        }
        return new RevenueBreakdown(breakdown);
    }

    public RevenueBreakdown getRevenueByStore(LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> results = eventRepository.sumRevenueGroupByStore(fromInstant, toInstant);
        Map<String, BigDecimal> breakdown = new LinkedHashMap<>();
        for (Object[] row : results) {
            breakdown.put((String) row[0], (BigDecimal) row[1]);
        }
        return new RevenueBreakdown(breakdown);
    }

    public List<TopProduct> getTopProducts(int limit, LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> results = eventRepository.findTopProductsByClicks(limit, fromInstant, toInstant);
        List<TopProduct> products = new ArrayList<>();
        for (Object[] row : results) {
            UUID productId = (UUID) row[0];
            String title = (String) row[1];
            long clicks = ((Number) row[2]).longValue();
            long conversions = ((Number) row[3]).longValue();
            BigDecimal revenue = (BigDecimal) row[4];
            products.add(new TopProduct(productId, title, clicks, conversions, revenue,
                calculateConversionRate(clicks, conversions)));
        }
        return products;
    }

    public ConversionFunnel getConversionFunnel(LocalDate from, LocalDate to) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Object[]> results = eventRepository.getConversionFunnel(fromInstant, toInstant);
        if (results.isEmpty()) {
            return new ConversionFunnel(0, 0, 0, 0.0, 0.0);
        }
        Object[] row = results.get(0);
        long views = ((Number) row[0]).longValue();
        long clicks = ((Number) row[1]).longValue();
        long conversions = ((Number) row[2]).longValue();

        double viewsToClicksDropoff = views > 0
            ? (double) (views - clicks) / views * 100.0
            : 0.0;
        double clicksToConversionsDropoff = clicks > 0
            ? (double) (clicks - conversions) / clicks * 100.0
            : 0.0;

        return new ConversionFunnel(views, clicks, conversions,
            viewsToClicksDropoff, clicksToConversionsDropoff);
    }

    public RealTimeMetrics getRealTimeMetrics() {
        Instant since = Instant.now().minus(24, java.time.temporal.ChronoUnit.HOURS);
        List<Object[]> results = eventRepository.getRealTimeMetrics(since);
        if (results.isEmpty()) {
            return new RealTimeMetrics(0, 0, BigDecimal.ZERO, 0);
        }
        Object[] row = results.get(0);
        long clicks = ((Number) row[0]).longValue();
        long conversions = ((Number) row[1]).longValue();
        BigDecimal revenue = (BigDecimal) row[2];
        long activeProducts = ((Number) row[3]).longValue();
        return new RealTimeMetrics(clicks, conversions, revenue, activeProducts);
    }

    public double calculateCTR(long views, long clicks) {
        if (views == 0) {
            return 0.0;
        }
        return Math.round((double) clicks / (double) views * 10000.0) / 100.0;
    }

    public double calculateConversionRate(long clicks, long conversions) {
        if (clicks == 0) {
            return 0.0;
        }
        return Math.round((double) conversions / (double) clicks * 10000.0) / 100.0;
    }

    private double calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
}
