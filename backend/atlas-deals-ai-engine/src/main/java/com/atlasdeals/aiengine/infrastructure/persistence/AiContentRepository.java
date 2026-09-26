package com.atlasdeals.aiengine.infrastructure.persistence;

import com.atlasdeals.aiengine.domain.model.AiContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiContentRepository extends JpaRepository<AiContent, UUID> {

    Optional<AiContent> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);
}
