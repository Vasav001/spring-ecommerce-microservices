package com.vasav.ecom.integrationservice.entity;

import com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment")
@Getter
@Setter
@NoArgsConstructor
public class Shipment {

    @Id
    private UUID orderId;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    private String failureReason;

    private Instant createdAt;

    private Instant updatedAt;
}
