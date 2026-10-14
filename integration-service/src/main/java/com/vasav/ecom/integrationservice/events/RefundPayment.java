package com.vasav.ecom.integrationservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundPayment(
        UUID orderId,
        BigDecimal amount
) {
}
