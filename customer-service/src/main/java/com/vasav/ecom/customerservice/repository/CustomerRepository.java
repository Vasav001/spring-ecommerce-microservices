package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    @Query("SELECT c FROM Customer c JOIN FETCH c.user WHERE c.user.keycloakUserId = :keycloakUserId")
    Optional<Customer> findByUserKeycloakUserId(@Param("keycloakUserId") String keycloakUserId);

    @Query("SELECT c FROM Customer c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Customer> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT c FROM Customer c JOIN FETCH c.user WHERE c.user.email = :email")
    Optional<Customer> findByUserEmail(@Param("email") String email);

    @Query("SELECT c FROM Customer c JOIN FETCH c.user WHERE c.user.username = :username")
    Optional<Customer> findByUserUsername(@Param("username") String username);

    Optional<Customer> findByUserId(UUID userId);

    boolean existsByUserKeycloakUserId(String keycloakUserId);

    boolean existsByUserId(UUID userId);

    @Query(value = "SELECT c FROM Customer c JOIN FETCH c.user",
            countQuery = "SELECT count(c) FROM Customer c")
    Page<Customer> findAllWithUser(Pageable pageable);

    @Query(value = "SELECT c FROM Customer c JOIN FETCH c.user WHERE c.active = :active",
            countQuery = "SELECT count(c) FROM Customer c WHERE c.active = :active")
    Page<Customer> findAllByActiveWithUser(@Param("active") boolean active, Pageable pageable);
}
