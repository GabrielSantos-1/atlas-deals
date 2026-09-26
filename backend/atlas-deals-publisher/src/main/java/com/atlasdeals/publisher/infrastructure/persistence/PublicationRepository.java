package com.atlasdeals.publisher.infrastructure.persistence;

import com.atlasdeals.publisher.domain.model.Publication;
import com.atlasdeals.publisher.domain.model.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, UUID> {

    List<Publication> findByProductIdOrderByCreatedAtDesc(UUID productId);

    List<Publication> findByStatusIn(List<PublicationStatus> statuses);

    List<Publication> findByStatusAndScheduledForBefore(PublicationStatus status, Instant now);

    @Query("SELECT p FROM Publication p WHERE p.status = :status AND p.retryCount < p.maxRetries")
    List<Publication> findByStatusAndRetryCountLessThanMax(@Param("status") PublicationStatus status);

    List<Publication> findByPlatform(String platform);

    long countByPlatformAndStatus(String platform, PublicationStatus status);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.platform = :platform AND p.status = :status AND p.publishedAt >= :since")
    long countByPlatformAndStatusSince(@Param("platform") String platform,
                                       @Param("status") PublicationStatus status,
                                       @Param("since") Instant since);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (p.published_at - p.created_at))) FROM publications p WHERE p.platform = :platform AND p.status = :status", nativeQuery = true)
    Double avgPublishTimeSeconds(@Param("platform") String platform,
                                 @Param("status") String status);
}
