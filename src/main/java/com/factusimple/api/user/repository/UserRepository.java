package com.factusimple.api.user.repository;

import com.factusimple.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.invoiceCount = u.invoiceCount + 1 WHERE u.id = :userId")
    int incrementInvoiceCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.invoiceCount = u.invoiceCount - 1 WHERE u.id = :userId AND u.invoiceCount > 0")
    int decrementInvoiceCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.customersCount = u.customersCount + 1 WHERE u.id = :userId")
    int incrementCustomersCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.customersCount = u.customersCount - 1 WHERE u.id = :userId AND u.customersCount > 0")
    int decrementCustomersCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.productsCount = u.productsCount + 1 WHERE u.id = :userId")
    int incrementProductsCount(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE User u SET u.productsCount = u.productsCount - 1 WHERE u.id = :userId AND u.productsCount > 0")
    int decrementProductsCount(@Param("userId") UUID userId);
}
