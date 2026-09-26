package com.atlasdeals.analytics.domain.repository;

import com.atlasdeals.analytics.domain.model.AnalyticsEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, UUID> {

    List<AnalyticsEvent> findByEventTimeBetween(Instant from, Instant to);

    @Query(value = "SELECT COUNT(*) FROM analytics_events WHERE event_type = :type AND event_time BETWEEN :from AND :to", nativeQuery = true)
    long countByEventTypeAndTimeBetween(@Param("type") String type, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT COALESCE(SUM(revenue), 0) FROM analytics_events WHERE event_time BETWEEN :from AND :to", nativeQuery = true)
    BigDecimal sumRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT COUNT(DISTINCT product_id) FROM analytics_events WHERE event_time BETWEEN :from AND :to", nativeQuery = true)
    long countDistinctProductIdsBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT platform, COALESCE(SUM(revenue), 0) FROM analytics_events WHERE event_time BETWEEN :from AND :to GROUP BY platform ORDER BY SUM(revenue) DESC", nativeQuery = true)
    List<Object[]> sumRevenueGroupByPlatform(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT COALESCE(category, 'Unknown'), COALESCE(SUM(revenue), 0) FROM analytics_events WHERE event_time BETWEEN :from AND :to GROUP BY category ORDER BY SUM(revenue) DESC", nativeQuery = true)
    List<Object[]> sumRevenueGroupByCategory(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT COALESCE(store_name, 'Unknown'), COALESCE(SUM(revenue), 0) FROM analytics_events WHERE event_time BETWEEN :from AND :to GROUP BY store_name ORDER BY SUM(revenue) DESC", nativeQuery = true)
    List<Object[]> sumRevenueGroupByStore(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
        SELECT product_id, COALESCE(product_title, 'Unknown'),
               COUNT(*) FILTER (WHERE event_type = 'CLICK') as clicks,
               COUNT(*) FILTER (WHERE event_type = 'CONVERSION') as conversions,
               COALESCE(SUM(revenue), 0) as revenue
        FROM analytics_events
        WHERE event_time BETWEEN :from AND :to
        GROUP BY product_id, product_title
        ORDER BY clicks DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopProductsByClicks(@Param("limit") int limit, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
        SELECT product_id, COALESCE(product_title, 'Unknown'),
               COUNT(*) FILTER (WHERE event_type = 'CLICK') as clicks,
               COUNT(*) FILTER (WHERE event_type = 'CONVERSION') as conversions,
               COALESCE(SUM(revenue), 0) as revenue
        FROM analytics_events
        WHERE event_time BETWEEN :from AND :to
        GROUP BY product_id, product_title
        ORDER BY conversions DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopProductsByConversions(@Param("limit") int limit, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
        SELECT product_id, COALESCE(product_title, 'Unknown'),
               COUNT(*) FILTER (WHERE event_type = 'CLICK') as clicks,
               COUNT(*) FILTER (WHERE event_type = 'CONVERSION') as conversions,
               COALESCE(SUM(revenue), 0) as revenue
        FROM analytics_events
        WHERE event_time BETWEEN :from AND :to
        GROUP BY product_id, product_title
        ORDER BY revenue DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopProductsByRevenue(@Param("limit") int limit, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
        SELECT
            COALESCE(SUM(CASE WHEN event_type = 'VIEW' THEN 1 ELSE 0 END), 0) as views,
            COALESCE(SUM(CASE WHEN event_type = 'CLICK' THEN 1 ELSE 0 END), 0) as clicks,
            COALESCE(SUM(CASE WHEN event_type = 'CONVERSION' THEN 1 ELSE 0 END), 0) as conversions
        FROM analytics_events
        WHERE event_time BETWEEN :from AND :to
        """, nativeQuery = true)
    List<Object[]> getConversionFunnel(@Param("from") Instant from, @Param("to") Instant to);

    @Query(value = """
        SELECT
            COALESCE(SUM(CASE WHEN event_type = 'CLICK' THEN 1 ELSE 0 END), 0) as clicks,
            COALESCE(SUM(CASE WHEN event_type = 'CONVERSION' THEN 1 ELSE 0 END), 0) as conversions,
            COALESCE(SUM(revenue), 0) as revenue,
            COUNT(DISTINCT product_id) as activeProducts
        FROM analytics_events
        WHERE event_time >= :since
        """, nativeQuery = true)
    List<Object[]> getRealTimeMetrics(@Param("since") Instant since);
}
