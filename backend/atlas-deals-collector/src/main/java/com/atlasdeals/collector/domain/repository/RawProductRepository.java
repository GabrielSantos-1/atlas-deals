package com.atlasdeals.collector.domain.repository;

import com.atlasdeals.collector.domain.model.RawProduct;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawProductRepository extends JpaRepository<RawProduct, UUID> {

    Optional<RawProduct> findByExternalIdAndStoreName(String externalId, String storeName);

    Optional<RawProduct> findByProductUrlHash(int hash);

    Page<RawProduct> findByCategory(String category, Pageable pageable);

    long countByStoreName(String storeName);
}
