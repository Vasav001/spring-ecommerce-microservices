package com.vasav.ecom.inventoryservice.service;

import com.vasav.ecom.inventoryservice.entity.enums.ReservationStatus;
import com.vasav.ecom.inventoryservice.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpiryJob {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "${inventory.reservation.expiry-interval:30s}")
    public void expireReservations() {
        for (UUID orderId : reservationRepository.findOrderIdsExpiredBefore(
                ReservationStatus.RESERVED, Instant.now(), Limit.of(100))) {
            try {
                reservationService.expire(orderId);
            } catch (Exception e) {
                log.error("Failed to expire reservation for order {}", orderId, e);
            }
        }
    }
}
