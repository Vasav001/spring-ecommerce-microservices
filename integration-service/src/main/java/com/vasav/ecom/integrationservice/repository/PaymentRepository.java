package com.vasav.ecom.integrationservice.repository;

import com.vasav.ecom.integrationservice.entity.Payment;
import com.vasav.ecom.integrationservice.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderIdAndCustomerId(UUID orderId, String customerId);

    @Modifying
    @Query(value = """
            INSERT INTO payment (order_id, customer_id, amount, status)
            VALUES (:orderId, :customerId, :amount, 'PENDING')
            ON CONFLICT (order_id) DO NOTHING""", nativeQuery = true)
    int createPending(@Param("orderId") UUID orderId, @Param("customerId") String customerId,
                      @Param("amount") BigDecimal amount);

    @Modifying
    @Query(value = """
            UPDATE payment SET status = CASE status WHEN 'SUCCEEDED' THEN 'REFUNDED' ELSE 'CANCELLED' END,
                               updated_at = now()
            WHERE order_id = :orderId AND status IN ('PENDING', 'SUCCEEDED')""", nativeQuery = true)
    int refund(@Param("orderId") UUID orderId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Payment p set p.status = :to, p.failureReason = :reason, p.updatedAt = instant
            where p.orderId = :orderId and p.status = :from""")
    int updateStatus(@Param("orderId") UUID orderId, @Param("from") PaymentStatus from,
                     @Param("to") PaymentStatus to, @Param("reason") String reason);
}
