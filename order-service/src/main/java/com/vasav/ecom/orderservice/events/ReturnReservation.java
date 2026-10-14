package com.vasav.ecom.orderservice.events;

import java.util.UUID;

public record ReturnReservation(
        UUID orderId
) {
}
