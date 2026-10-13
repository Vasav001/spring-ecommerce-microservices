package com.vasav.ecom.inventoryservice.service;

import com.vasav.ecom.inventoryservice.entity.Reservation;
import com.vasav.ecom.inventoryservice.entity.enums.ReservationStatus;
import com.vasav.ecom.inventoryservice.events.EventTypes;
import com.vasav.ecom.inventoryservice.events.ReserveInventory;
import com.vasav.ecom.inventoryservice.producer.InventoryReplyProducer;
import com.vasav.ecom.inventoryservice.producer.StockLevelProducer;
import com.vasav.ecom.inventoryservice.repository.ProcessedEventRepository;
import com.vasav.ecom.inventoryservice.repository.ReservationRepository;
import com.vasav.ecom.inventoryservice.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String CONSUMER = "inventory-service.commands";

    private final StockItemRepository stockItemRepository;
    private final ReservationRepository reservationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryReplyProducer replyProducer;
    private final StockLevelProducer stockLevelProducer;

    @Value("${inventory.reservation.ttl:15m}")
    private Duration ttl;

//    reserve, confirm, release, expire

    @Transactional
    public void reserve(UUID eventId, ReserveInventory command) {
        // idempotency
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        UUID orderId = command.orderId();

        ReservationStatus existing = currentStatus(orderId);
        if (existing != null) {
            // if reservation exist, reply sends fail msg via outbox with current status as reason
            replyReserved(orderId, existing == ReservationStatus.RESERVED || existing == ReservationStatus.CONFIRMED, existing.name());
            return;
        }

        if (command.items().isEmpty() || command.items().stream().anyMatch(item -> item.quantity() <= 0)) {
            replyReserved(orderId, false, "INVALID_REQUEST");
            return;
        }

        Map<UUID, Integer> quantities = new TreeMap<>();
        command.items().forEach(item -> quantities.merge(item.productId(), item.quantity(), Integer::sum));

//        if became 0 then publish, if became non 0 from 0 then pusblish
        boolean reserved = stockLevelProducer.publishOnFlip(quantities.keySet(), () -> reserveAll(quantities));
        if (reserved) {
            Instant expiresAt = Instant.now().plus(ttl);
            quantities.forEach((productId, qty) ->
                    reservationRepository.save(new Reservation(orderId, productId, qty, expiresAt)));
        }
        replyReserved(orderId, reserved, "INSUFFICIENT_STOCK");
    }

    @Transactional
    public void confirm(UUID eventId, UUID orderId) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        if (reservationRepository.updateStatus(orderId, ReservationStatus.RESERVED, ReservationStatus.CONFIRMED) > 0) {
            reservationRepository.findByOrderId(orderId)
                    .forEach(r -> stockItemRepository.confirm(r.getProductId(), r.getQuantity()));
        }
        ReservationStatus status = currentStatus(orderId);
        if (status == ReservationStatus.CONFIRMED) {
            replyProducer.reply(orderId, EventTypes.RESERVATION_CONFIRMED);
        } else {
            replyProducer.reply(orderId, EventTypes.RESERVATION_CONFIRM_FAILED, status == null ? "NOT_FOUND" : status.name());
        }
    }

    @Transactional
    public void release(UUID eventId, UUID orderId) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        returnStock(orderId, ReservationStatus.RELEASED);
        if (currentStatus(orderId) == ReservationStatus.CONFIRMED) {
            replyProducer.reply(orderId, EventTypes.RESERVATION_RELEASE_FAILED, "ALREADY_CONFIRMED");
        } else {
            replyProducer.reply(orderId, EventTypes.RESERVATION_RELEASED);
        }
    }

    @Transactional
    public void expire(UUID orderId) {
        if (returnStock(orderId, ReservationStatus.EXPIRED)) {
            replyProducer.reply(orderId, EventTypes.RESERVATION_EXPIRED);
        }
    }

    // reserves every product or none: on the first shortage, undo what was reserved so far
    private boolean reserveAll(Map<UUID, Integer> quantities) {
        List<UUID> done = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : quantities.entrySet()) {
            if (stockItemRepository.reserve(entry.getKey(), entry.getValue()) == 0) {
                done.forEach(id -> stockItemRepository.release(id, quantities.get(id)));
                return false;
            }
            done.add(entry.getKey());
        }
        return true;
    }

    // moves a RESERVED order to RELEASED / EXPIRED and gives its stock back; false if it was not RESERVED
    private boolean returnStock(UUID orderId, ReservationStatus to) {
        if (reservationRepository.updateStatus(orderId, ReservationStatus.RESERVED, to) == 0) {
            return false;
        }
        List<Reservation> lines = reservationRepository.findByOrderId(orderId);
        stockLevelProducer.publishOnFlip(lines.stream().map(Reservation::getProductId).toList(), () -> {
            lines.forEach(r -> stockItemRepository.release(r.getProductId(), r.getQuantity()));
            return null;
        });
        return true;
    }

    private void replyReserved(UUID orderId, boolean reserved, String failureReason) {
        if (reserved) {
            replyProducer.reply(orderId, EventTypes.INVENTORY_RESERVED);
        } else {
            replyProducer.reply(orderId, EventTypes.INVENTORY_RESERVATION_FAILED, failureReason);
        }
    }

    private ReservationStatus currentStatus(UUID orderId) {
        List<Reservation> lines = reservationRepository.findByOrderId(orderId);
        return lines.isEmpty() ? null : lines.getFirst().getStatus();
    }
}
