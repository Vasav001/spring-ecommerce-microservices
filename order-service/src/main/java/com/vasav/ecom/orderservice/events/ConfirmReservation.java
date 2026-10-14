package com.vasav.ecom.orderservice.events;

import java.util.UUID;

public record ConfirmReservation(
        UUID orderId
) {
}
