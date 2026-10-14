package com.vasav.ecom.orderservice.events;

import java.util.UUID;

public record OrderFailed(
        UUID orderId,
        String customerId,
        String reason
) {
}
