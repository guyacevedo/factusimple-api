package com.factusimple.api.product.repository;

import com.factusimple.api.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByEstablishmentId(UUID establishmentId, Pageable pageable);

    Optional<Product> findByIdAndEstablishmentId(UUID id, UUID establishmentId);

    boolean existsBySkuAndEstablishmentId(String sku, UUID establishmentId);
}
