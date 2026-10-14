package com.vasav.ecom.orderservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundPayment(
        UUID orderId,
        BigDecimal amount
) {
}
