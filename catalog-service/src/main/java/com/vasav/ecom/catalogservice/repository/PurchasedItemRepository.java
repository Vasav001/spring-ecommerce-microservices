package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.PurchasedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PurchasedItemRepository extends JpaRepository<PurchasedItem, PurchasedItem.Key> {

    boolean existsByCustomerIdAndIdProductIdAndDeliveredTrue(String customerId, UUID productId);

    @Modifying
    @Query(value = """
            INSERT INTO purchased_item (order_id, product_id, customer_id)
            VALUES (:orderId, :productId, :customerId)
            ON CONFLICT (order_id, product_id) DO NOTHING""", nativeQuery = true)
    int record(@Param("orderId") UUID orderId, @Param("productId") UUID productId,
               @Param("customerId") String customerId);

    @Modifying
    @Query("update PurchasedItem p set p.delivered = true where p.id.orderId = :orderId")
    int markDelivered(@Param("orderId") UUID orderId);

    @Modifying
    @Query("delete from PurchasedItem p where p.id.orderId = :orderId")
    int deleteByOrderId(@Param("orderId") UUID orderId);
}
