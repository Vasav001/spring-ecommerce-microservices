package com.vasav.ecom.orderservice.repository;

import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndCustomerId(UUID id, String customerId);

    Optional<Order> findByCustomerIdAndIdempotencyKey(String customerId, String idempotencyKey);

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Order o set o.status = :to, o.updatedAt = instant where o.id = :id and o.status = :from")
    int updateStatus(@Param("id") UUID id, @Param("from") OrderStatus from, @Param("to") OrderStatus to);
}
