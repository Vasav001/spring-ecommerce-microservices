package com.vasav.ecom.orderservice.events;

import java.util.UUID;

public record OrderShipped(
        UUID orderId,
        String customerId
) {
}
