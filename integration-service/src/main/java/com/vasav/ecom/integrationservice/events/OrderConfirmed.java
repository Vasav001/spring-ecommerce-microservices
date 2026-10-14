package com.vasav.ecom.integrationservice.events;

import java.util.UUID;

public record OrderConfirmed(
        UUID orderId,
        String customerId
) {
}
