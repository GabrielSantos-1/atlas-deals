package com.atlasdeals.promotionengine.domain.repository;

import com.atlasdeals.promotionengine.domain.model.Promotion;
import com.atlasdeals.promotionengine.domain.model.PromotionStatus;
import java.util.List;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    List<Promotion> findByStatus(PromotionStatus status);

    Optional<Promotion> findByProductId(UUID productId);

    List<Promotion> findByStatusIn(List<PromotionStatus> statuses);

    List<Promotion> findByCategoryAndStatus(String category, PromotionStatus status, Pageable pageable);

    @Query("SELECT p FROM Promotion p ORDER BY p.score DESC")
    List<Promotion> findTopByOrderByScoreDesc(Pageable pageable);

    long countByStatus(PromotionStatus status);

    @Query("SELECT COUNT(p) FROM Promotion p")
    long countTotal();

    boolean existsByProductIdAndStatus(UUID productId, PromotionStatus status);

    List<Promotion> findByPublishedAtBeforeAndStatus(Instant instant, PromotionStatus status);

    List<Promotion> findByStatusAndPublishedAtNotNull(PromotionStatus status);
}
