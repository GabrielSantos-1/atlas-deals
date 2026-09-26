package com.atlasdeals.pricehistory.application.port;

import com.atlasdeals.pricehistory.domain.model.PriceRecord;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceHistoryRepository {

    PriceRecord save(PriceRecord record);

    Optional<PriceRecord> findLatestByProductId(UUID productId);

    List<PriceRecord> findByProductId(UUID productId, Pageable pageable);

    List<PriceRecord> findByProductIdAndDateRange(UUID productId, Instant from, Instant to);

    List<PriceRecord> findByProductIdSince(UUID productId, Instant since);

    Optional<BigDecimal> findLowestPriceByProductId(UUID productId);

    Optional<BigDecimal> findMinPriceByProductIdAndDateRange(UUID productId, Instant start, Instant end);

    Optional<BigDecimal> findMaxPriceByProductIdAndDateRange(UUID productId, Instant start, Instant end);

    Optional<BigDecimal> findAvgPriceByProductIdAndDateRange(UUID productId, Instant start, Instant end);

    List<BigDecimal> findPricesByProductIdAndDateRangeAsc(UUID productId, Instant start, Instant end);

    long countByProductId(UUID productId);
}
