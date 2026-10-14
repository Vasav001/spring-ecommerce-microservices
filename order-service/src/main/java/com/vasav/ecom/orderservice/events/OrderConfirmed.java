package com.vasav.ecom.orderservice.events;

import com.vasav.ecom.orderservice.dto.ShippingAddress;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderConfirmed(
        UUID orderId,
        String customerId,
        BigDecimal totalAmount,
        List<Item> items,
        ShippingAddress shippingAddress
) {
    public record Item(
            UUID productId,
            String productName,
            BigDecimal unitPrice,
            int quantity
    ) {
    }
}
