package com.vasav.ecom.integrationservice.dto;

import com.vasav.ecom.integrationservice.entity.Payment;
import com.vasav.ecom.integrationservice.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID orderId,
        BigDecimal amount,
        PaymentStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getOrderId(), payment.getAmount(), payment.getStatus(),
                payment.getFailureReason(), payment.getCreatedAt(), payment.getUpdatedAt());
    }
}
