package com.factusimple.api.user.repository;

import com.factusimple.api.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"plan"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.invoiceCount = u.invoiceCount + 1 WHERE u.id = :userId")
    int incrementInvoiceCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.invoiceCount = u.invoiceCount - 1 WHERE u.id = :userId AND u.invoiceCount > 0")
    int decrementInvoiceCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.customersCount = u.customersCount + 1 WHERE u.id = :userId")
    int incrementCustomersCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.customersCount = u.customersCount - 1 WHERE u.id = :userId AND u.customersCount > 0")
    int decrementCustomersCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.productsCount = u.productsCount + 1 WHERE u.id = :userId")
    int incrementProductsCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.productsCount = u.productsCount - 1 WHERE u.id = :userId AND u.productsCount > 0")
    int decrementProductsCount(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.invoiceCount = u.invoiceCount + 1 WHERE u.id = :userId AND u.invoiceCount < :maxInvoices")
    int incrementInvoiceCountIfBelowLimit(@Param("userId") UUID userId, @Param("maxInvoices") int maxInvoices);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.customersCount = u.customersCount + 1 WHERE u.id = :userId AND u.customersCount < :maxCustomers")
    int incrementCustomersCountIfBelowLimit(@Param("userId") UUID userId, @Param("maxCustomers") int maxCustomers);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.productsCount = u.productsCount + 1 WHERE u.id = :userId AND u.productsCount < :maxProducts")
    int incrementProductsCountIfBelowLimit(@Param("userId") UUID userId, @Param("maxProducts") int maxProducts);
}
