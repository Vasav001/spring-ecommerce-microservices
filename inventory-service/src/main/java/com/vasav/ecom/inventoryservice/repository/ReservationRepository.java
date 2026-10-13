package com.vasav.ecom.inventoryservice.repository;

import com.vasav.ecom.inventoryservice.entity.Reservation;
import com.vasav.ecom.inventoryservice.entity.enums.ReservationStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByOrderId(UUID orderId);

    @Query("select distinct r.orderId from Reservation r where r.status = :status and r.expiresAt < :now")
    List<UUID> findOrderIdsExpiredBefore(@Param("status") ReservationStatus status, @Param("now") Instant now, Limit limit);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Reservation r set r.status = :to where r.orderId = :orderId and r.status = :from")
    int updateStatus(@Param("orderId") UUID orderId, @Param("from") ReservationStatus from, @Param("to") ReservationStatus to);
}
