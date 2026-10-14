package com.vasav.ecom.integrationservice.dto;

import com.vasav.ecom.integrationservice.entity.Shipment;
import com.vasav.ecom.integrationservice.entity.enums.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(
        UUID orderId,
        ShipmentStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(shipment.getOrderId(), shipment.getStatus(), shipment.getFailureReason(),
                shipment.getCreatedAt(), shipment.getUpdatedAt());
    }
}
