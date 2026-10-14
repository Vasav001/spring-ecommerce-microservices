package com.vasav.ecom.orderservice.repository;

import com.vasav.ecom.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndCustomerId(UUID id, String customerId);

    Optional<Order> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
}
