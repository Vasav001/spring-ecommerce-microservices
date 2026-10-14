package com.vasav.ecom.integrationservice.repository;

import com.vasav.ecom.integrationservice.entity.Shipment;
import com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrderIdAndCustomerId(UUID orderId, String customerId);

    @Modifying
    @Query(value = """
            INSERT INTO shipment (order_id, customer_id, status)
            VALUES (:orderId, :customerId, 'CREATED')
            ON CONFLICT (order_id) DO NOTHING""", nativeQuery = true)
    int create(@Param("orderId") UUID orderId, @Param("customerId") String customerId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Shipment s set s.status = :to, s.failureReason = :reason, s.updatedAt = instant
            where s.orderId = :orderId and s.status in :from""")
    int updateStatus(@Param("orderId") UUID orderId, @Param("from") Collection<ShipmentStatus> from,
                     @Param("to") ShipmentStatus to, @Param("reason") String reason);
}
