package com.vasav.ecom.orderservice.events;

import java.util.UUID;

public record OrderDelivered(
        UUID orderId,
        String customerId
) {
}
