package com.atlasdeals.pricehistory.domain.repository;

import com.atlasdeals.pricehistory.domain.model.PriceRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PriceRecordRepository extends JpaRepository<PriceRecord, UUID> {

    Optional<PriceRecord> findFirstByProductIdOrderByRecordedAtDesc(UUID productId);

    List<PriceRecord> findByProductIdOrderByRecordedAtDesc(UUID productId, Pageable pageable);

    List<PriceRecord> findByProductIdAndRecordedAtBetween(UUID productId, Instant from, Instant to);

    List<PriceRecord> findByProductIdAndRecordedAtAfter(UUID productId, Instant since);

    @Query("SELECT MIN(pr.price) FROM PriceRecord pr WHERE pr.productId = :productId")
    Optional<BigDecimal> findLowestPriceByProductId(@Param("productId") UUID productId);

    @Query("SELECT pr FROM PriceRecord pr WHERE pr.productId = :productId AND pr.recordedAt >= :start AND pr.recordedAt <= :end ORDER BY pr.recordedAt ASC")
    List<PriceRecord> findPricesByDateRange(@Param("productId") UUID productId,
                                            @Param("start") Instant start,
                                            @Param("end") Instant end);

    @Query("SELECT MIN(pr.price) FROM PriceRecord pr WHERE pr.productId = :productId AND pr.recordedAt >= :start AND pr.recordedAt <= :end")
    Optional<BigDecimal> findMinPriceByProductIdAndDateRange(@Param("productId") UUID productId,
                                                             @Param("start") Instant start,
                                                             @Param("end") Instant end);

    @Query("SELECT pr.price FROM PriceRecord pr WHERE pr.productId = :productId AND pr.recordedAt >= :start AND pr.recordedAt <= :end ORDER BY pr.recordedAt ASC")
    List<BigDecimal> findPricesByProductIdAndDateRangeAsc(@Param("productId") UUID productId,
                                                          @Param("start") Instant start,
                                                          @Param("end") Instant end);

    @Query("SELECT MAX(pr.price) FROM PriceRecord pr WHERE pr.productId = :productId AND pr.recordedAt >= :start AND pr.recordedAt <= :end")
    Optional<BigDecimal> findMaxPriceByProductIdAndDateRange(@Param("productId") UUID productId,
                                                             @Param("start") Instant start,
                                                             @Param("end") Instant end);

    @Query("SELECT AVG(pr.price) FROM PriceRecord pr WHERE pr.productId = :productId AND pr.recordedAt >= :start AND pr.recordedAt <= :end")
    Optional<BigDecimal> findAvgPriceByProductIdAndDateRange(@Param("productId") UUID productId,
                                                             @Param("start") Instant start,
                                                             @Param("end") Instant end);

    long countByProductId(UUID productId);
}
