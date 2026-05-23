package com.factusimple.api.product.repository;

import com.factusimple.api.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"establishment"})
    Page<Product> findByEstablishmentId(UUID establishmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"establishment"})
    Optional<Product> findByIdAndEstablishmentId(UUID id, UUID establishmentId);

    boolean existsBySkuAndEstablishmentId(String sku, UUID establishmentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " +
           "WHERE p.id = :productId AND p.stock IS NOT NULL AND p.stock >= :quantity")
    int decrementStock(@Param("productId") UUID productId, @Param("quantity") BigDecimal quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity " +
           "WHERE p.id = :productId AND p.stock IS NOT NULL")
    int incrementStock(@Param("productId") UUID productId, @Param("quantity") BigDecimal quantity);
}
