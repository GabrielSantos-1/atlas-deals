package com.atlasdeals.analytics.domain.repository;

import com.atlasdeals.analytics.domain.model.DailyMetric;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyMetricRepository extends JpaRepository<DailyMetric, UUID> {

    Optional<DailyMetric> findByDateAndPlatform(LocalDate date, String platform);

    List<DailyMetric> findByDateBetween(LocalDate from, LocalDate to);

    List<DailyMetric> findByDateBetweenAndPlatform(LocalDate from, LocalDate to, String platform);
}
