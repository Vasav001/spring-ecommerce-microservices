package com.vasav.ecom.integrationservice.events;

import java.util.UUID;

public record ShipmentEvent(
        UUID orderId,
        String reason
) {
}
