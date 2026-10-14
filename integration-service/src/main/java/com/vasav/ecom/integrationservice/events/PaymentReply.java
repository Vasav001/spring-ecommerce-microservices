package com.vasav.ecom.integrationservice.events;

import java.util.UUID;

public record PaymentReply(
        UUID orderId,
        String reason
) {
}
