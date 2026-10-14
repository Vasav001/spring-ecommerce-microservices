package com.vasav.ecom.orderservice.events;

import java.util.UUID;

public record SagaReply(
        UUID orderId,
        String reason
) {
}
