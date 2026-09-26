package com.atlasdeals.analytics.infrastructure.scheduler;

import com.atlasdeals.analytics.domain.model.AnalyticsEvent;
import com.atlasdeals.analytics.domain.model.DailyMetric;
import com.atlasdeals.analytics.domain.repository.AnalyticsEventRepository;
import com.atlasdeals.analytics.domain.repository.DailyMetricRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MetricsAggregationScheduler {

    private static final Logger log = LoggerFactory.getLogger(MetricsAggregationScheduler.class);

    private final AnalyticsEventRepository eventRepository;
    private final DailyMetricRepository dailyMetricRepository;

    public MetricsAggregationScheduler(AnalyticsEventRepository eventRepository,
                                       DailyMetricRepository dailyMetricRepository) {
        this.eventRepository = eventRepository;
        this.dailyMetricRepository = dailyMetricRepository;
    }

    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void aggregateDailyMetrics() {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        log.info("Starting daily metrics aggregation for {}", yesterday);

        Instant dayStart = yesterday.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd = yesterday.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<AnalyticsEvent> events = eventRepository.findByEventTimeBetween(dayStart, dayEnd);

        if (events.isEmpty()) {
            log.info("No events found for {}", yesterday);
            return;
        }

        Map<String, List<AnalyticsEvent>> byPlatform = events.stream()
            .collect(Collectors.groupingBy(AnalyticsEvent::getPlatform));

        for (Map.Entry<String, List<AnalyticsEvent>> entry : byPlatform.entrySet()) {
            String platform = entry.getKey();
            List<AnalyticsEvent> platformEvents = entry.getValue();

            aggregateForPlatform(yesterday, platform, null, null, platformEvents);
        }

        log.info("Completed daily metrics aggregation for {} with {} platforms", yesterday, byPlatform.size());
    }

    private void aggregateForPlatform(LocalDate date, String platform, String category,
                                       String storeName, List<AnalyticsEvent> events) {
        Optional<DailyMetric> existing = dailyMetricRepository.findByDateAndPlatform(date, platform);
        DailyMetric metric = existing.orElseGet(() -> {
            DailyMetric m = new DailyMetric();
            m.setDate(date);
            m.setPlatform(platform);
            m.setCategory(category);
            m.setStoreName(storeName);
            m.setTotalRevenue(BigDecimal.ZERO);
            m.setRevenuePerClick(BigDecimal.ZERO);
            return m;
        });

        long totalClicks = 0;
        long totalConversions = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalViews = 0;
        long totalShares = 0;
        Set<String> uniqueProductSet = new HashSet<>();

        for (AnalyticsEvent event : events) {
            switch (event.getEventType()) {
                case "CLICK" -> totalClicks++;
                case "CONVERSION" -> {
                    totalConversions++;
                    if (event.getRevenue() != null) {
                        totalRevenue = totalRevenue.add(event.getRevenue());
                    }
                }
                case "VIEW" -> totalViews++;
                case "SHARE" -> totalShares++;
            }
            if (event.getProductId() != null) {
                uniqueProductSet.add(event.getProductId().toString());
            }
        }

        metric.setTotalClicks(totalClicks);
        metric.setTotalConversions(totalConversions);
        metric.setTotalRevenue(totalRevenue);
        metric.setTotalViews(totalViews);
        metric.setTotalShares(totalShares);
        metric.setCtr(calculateCTR(totalViews, totalClicks));
        metric.setConversionRate(calculateConversionRate(totalClicks, totalConversions));
        metric.setRevenuePerClick(
            totalClicks > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalClicks), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        metric.setUniqueProducts(uniqueProductSet.size());

        dailyMetricRepository.save(metric);
    }

    private double calculateCTR(long views, long clicks) {
        if (views == 0) return 0.0;
        return Math.round((double) clicks / (double) views * 10000.0) / 100.0;
    }

    private double calculateConversionRate(long clicks, long conversions) {
        if (clicks == 0) return 0.0;
        return Math.round((double) conversions / (double) clicks * 10000.0) / 100.0;
    }
}
