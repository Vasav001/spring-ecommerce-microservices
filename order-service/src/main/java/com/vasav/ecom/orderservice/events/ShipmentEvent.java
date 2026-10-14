package com.vasav.ecom.orderservice.events;

import java.util.UUID;

public record ShipmentEvent(
        UUID orderId
) {
}
