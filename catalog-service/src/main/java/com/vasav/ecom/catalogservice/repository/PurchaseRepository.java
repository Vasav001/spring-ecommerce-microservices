package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, Purchase.Key> {

    @Modifying
    @Query(value = """
            INSERT INTO purchase (user_id, product_id, order_id, created_at)
            VALUES (:userId, :productId, :orderId, now())
            ON CONFLICT DO NOTHING""", nativeQuery = true)
    int insertIfAbsent(String userId, UUID productId, UUID orderId);

    boolean existsByUserIdAndProductId(String userId, UUID productId);   // used when writing a review
}