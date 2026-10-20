package com.atlasdeals.publisher.infrastructure.persistence;

import com.atlasdeals.publisher.domain.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    /**
     * Inserts the marker only when the event has not been processed yet.
     *
     * @return 1 when the row was inserted by this call, 0 when the event was already marked
     */
    @Modifying(flushAutomatically = true)
    @Query(value = """
        INSERT INTO processed_event (event_id, consumer, processed_at)
        VALUES (CAST(:eventId AS uuid), :consumer, now())
        ON CONFLICT (event_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("eventId") String eventId, @Param("consumer") String consumer);
}
