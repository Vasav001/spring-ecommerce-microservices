package com.vasav.ecom.orderservice.dto;

import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        String failureReason,
        BigDecimal totalAmount,
        List<OrderLine> items,
        ShippingAddress shippingAddress,
        Instant createdAt
) {
    public record OrderLine(
            UUID productId,
            String productName,
            BigDecimal unitPrice,
            int quantity
    ) {
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getFailureReason(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(i -> new OrderLine(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity()))
                        .toList(),
                ShippingAddress.from(order),
                order.getCreatedAt()
        );
    }
}
