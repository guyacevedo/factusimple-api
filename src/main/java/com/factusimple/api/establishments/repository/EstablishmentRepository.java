package com.factusimple.api.establishments.repository;

import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstablishmentRepository extends JpaRepository<Establishment, UUID> {

    Optional<Establishment> findByUser(User user);

    Optional<Establishment> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByNit(String nit);
}