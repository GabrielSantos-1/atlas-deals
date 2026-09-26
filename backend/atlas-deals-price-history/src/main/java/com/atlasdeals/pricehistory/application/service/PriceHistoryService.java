package com.atlasdeals.pricehistory.application.service;

import com.atlasdeals.common.application.port.CacheService;
import com.atlasdeals.common.domain.Money;
import com.atlasdeals.common.domain.ProductId;
import com.atlasdeals.common.domain.events.PriceUpdatedEvent;
import com.atlasdeals.pricehistory.application.dto.PriceComparison;
import com.atlasdeals.pricehistory.application.dto.PriceStats;
import com.atlasdeals.pricehistory.application.dto.PriceStats.Trend;
import com.atlasdeals.pricehistory.domain.model.PriceRecord;
import com.atlasdeals.pricehistory.domain.repository.PriceRecordRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PriceHistoryService {

    private static final String CACHE_KEY_PREFIX = "price:latest:";

    private final PriceRecordRepository repository;
    private final CacheService cacheService;
    private final PriceUpdateNotifier publisher;

    public PriceHistoryService(PriceRecordRepository repository,
                               CacheService cacheService,
                               PriceUpdateNotifier publisher) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.publisher = publisher;
    }

    public PriceRecord recordPrice(PriceRecord record) {
        Optional<PriceRecord> previous = repository.findFirstByProductIdOrderByRecordedAtDesc(record.getProductId());

        PriceRecord saved = repository.save(record);

        if (previous.isPresent()) {
            BigDecimal prevPrice = previous.get().getPrice();
            if (prevPrice.compareTo(saved.getPrice()) != 0) {
                PriceUpdatedEvent event = new PriceUpdatedEvent(
                    ProductId.of(saved.getProductId()),
                    Money.BRL(prevPrice),
                    Money.BRL(saved.getPrice()),
                    saved.getRecordedAt()
                );
                publisher.notifyPriceUpdated(event);
            }
        }

        cacheService.set(CACHE_KEY_PREFIX + saved.getProductId(), saved, Duration.ofMinutes(5));

        return saved;
    }

    @Transactional(readOnly = true)
    public List<PriceRecord> getPriceHistory(UUID productId, Instant from, Instant to, Pageable pageable) {
        if (from != null && to != null) {
            return repository.findPricesByDateRange(productId, from, to);
        }
        return repository.findByProductIdOrderByRecordedAtDesc(productId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<PriceRecord> getLatestPrice(UUID productId) {
        String cacheKey = CACHE_KEY_PREFIX + productId;
        Optional<PriceRecord> cached = cacheService.get(cacheKey, PriceRecord.class);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<PriceRecord> fromDb = repository.findFirstByProductIdOrderByRecordedAtDesc(productId);
        fromDb.ifPresent(r -> cacheService.set(cacheKey, r, Duration.ofMinutes(5)));
        return fromDb;
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> getLowestPrice(UUID productId) {
        return repository.findLowestPriceByProductId(productId);
    }

    @Transactional(readOnly = true)
    public PriceStats getPriceStats(UUID productId, Instant from, Instant to) {
        Optional<PriceRecord> latest = getLatestPrice(productId);

        if (from == null || to == null) {
            if (to == null) to = Instant.now();
            if (from == null) from = to.minus(Duration.ofDays(30));
        }

        Instant effectiveFrom = from;
        Instant effectiveTo = to;

        BigDecimal lowest = repository.findMinPriceByProductIdAndDateRange(productId, effectiveFrom, effectiveTo)
            .orElse(BigDecimal.ZERO);
        BigDecimal highest = repository.findMaxPriceByProductIdAndDateRange(productId, effectiveFrom, effectiveTo)
            .orElse(BigDecimal.ZERO);
        BigDecimal avg = repository.findAvgPriceByProductIdAndDateRange(productId, effectiveFrom, effectiveTo)
            .orElse(BigDecimal.ZERO);

        long count = repository.countByProductId(productId);

        BigDecimal currentPrice = latest.map(PriceRecord::getPrice).orElse(BigDecimal.ZERO);
        BigDecimal percentageChange = BigDecimal.ZERO;
        Trend trend = Trend.STABLE;

        if (latest.isPresent() && count > 1) {
            List<BigDecimal> pricesAsc = repository.findPricesByProductIdAndDateRangeAsc(
                productId, effectiveFrom, effectiveTo);
            if (!pricesAsc.isEmpty() && pricesAsc.getFirst().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal earliestPrice = pricesAsc.getFirst();
                percentageChange = currentPrice.subtract(earliestPrice)
                    .divide(earliestPrice, 4, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_EVEN);

                if (percentageChange.compareTo(BigDecimal.ONE.negate()) < 0) {
                    trend = Trend.DOWN;
                } else if (percentageChange.compareTo(BigDecimal.ONE) > 0) {
                    trend = Trend.UP;
                } else {
                    trend = Trend.STABLE;
                }
            }
        }

        return new PriceStats(
            productId,
            currentPrice,
            lowest,
            highest,
            avg.setScale(2, RoundingMode.HALF_EVEN),
            percentageChange,
            trend,
            (int) count
        );
    }

    @Transactional(readOnly = true)
    public PriceComparison comparePrices(UUID productId1, UUID productId2) {
        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(Duration.ofDays(30));

        PriceStats stats1 = getPriceStats(productId1, thirtyDaysAgo, now);
        PriceStats stats2 = getPriceStats(productId2, thirtyDaysAgo, now);

        BigDecimal diff = stats1.currentPrice().subtract(stats2.currentPrice());
        BigDecimal pctDiff = BigDecimal.ZERO;
        if (stats2.currentPrice().compareTo(BigDecimal.ZERO) > 0) {
            pctDiff = diff.divide(stats2.currentPrice(), 4, RoundingMode.HALF_EVEN)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_EVEN);
        }

        String betterValue = stats1.currentPrice().compareTo(stats2.currentPrice()) <= 0
            ? productId1.toString()
            : productId2.toString();

        return new PriceComparison(
            productId1,
            productId2,
            stats1,
            stats2,
            diff.abs(),
            pctDiff.abs(),
            betterValue
        );
    }

    @FunctionalInterface
    public interface PriceUpdateNotifier {
        void notifyPriceUpdated(PriceUpdatedEvent event);
    }
}
