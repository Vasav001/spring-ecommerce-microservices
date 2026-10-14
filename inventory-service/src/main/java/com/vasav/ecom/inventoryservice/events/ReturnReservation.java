package com.vasav.ecom.inventoryservice.events;

import java.util.UUID;

public record ReturnReservation(
        UUID orderId
) {
}
