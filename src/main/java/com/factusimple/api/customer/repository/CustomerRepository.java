package com.factusimple.api.customer.repository;

import com.factusimple.api.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @EntityGraph(attributePaths = {"establishment"})
    Page<Customer> findByEstablishmentId(UUID establishmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"establishment"})
    Optional<Customer> findByIdAndEstablishmentId(UUID id, UUID establishmentId);

    boolean existsByIdentificationAndEstablishmentId(String identification, UUID establishmentId);
}
