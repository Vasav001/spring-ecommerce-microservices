package com.vasav.ecom.integrationservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPayment(
        UUID orderId,
        String customerId,
        BigDecimal amount
) {
}
