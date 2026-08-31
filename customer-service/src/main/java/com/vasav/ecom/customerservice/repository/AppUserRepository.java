package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByKeycloakUserId(String keycloakUserId);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUsername(String username);

    boolean existsByKeycloakUserId(String keycloakUserId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
