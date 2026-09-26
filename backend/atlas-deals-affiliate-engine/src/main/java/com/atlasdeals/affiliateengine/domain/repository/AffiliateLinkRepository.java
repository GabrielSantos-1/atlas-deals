package com.atlasdeals.affiliateengine.domain.repository;

import com.atlasdeals.affiliateengine.domain.model.AffiliateLink;
import com.atlasdeals.common.domain.AffiliateNetwork;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, UUID> {

    Optional<AffiliateLink> findByProductIdAndNetwork(UUID productId, AffiliateNetwork network);

    List<AffiliateLink> findByProductId(UUID productId);

    boolean existsByProductIdAndNetwork(UUID productId, AffiliateNetwork network);
}
