package com.vasav.ecom.inventoryservice.entity;

import com.vasav.ecom.inventoryservice.entity.enums.ReservationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID orderId;
    private UUID productId;
    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Instant expiresAt;

    @CreationTimestamp
    private Instant createdAt;

    public Reservation(UUID orderId, UUID productId, int quantity, Instant expiresAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
        this.expiresAt = expiresAt;
    }
}
