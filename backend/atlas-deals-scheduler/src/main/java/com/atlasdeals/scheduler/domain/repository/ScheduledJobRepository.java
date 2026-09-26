package com.atlasdeals.scheduler.domain.repository;

import com.atlasdeals.scheduler.domain.model.ScheduledJob;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, UUID> {

    List<ScheduledJob> findByEnabledTrue();

    Optional<ScheduledJob> findByName(String name);
}
