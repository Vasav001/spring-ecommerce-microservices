package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("SELECT c FROM Customer c JOIN FETCH c.user WHERE c.user.keycloakUserId = :keycloakUserId")
    Optional<Customer> findByUserKeycloakUserId(@Param("keycloakUserId") String keycloakUserId);

    @Query("SELECT c FROM Customer c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Customer> findByIdWithUser(@Param("id") UUID id);

    @Query(value = "SELECT c FROM Customer c JOIN FETCH c.user",
            countQuery = "SELECT count(c) FROM Customer c")
    Page<Customer> findAllWithUser(Pageable pageable);
}
