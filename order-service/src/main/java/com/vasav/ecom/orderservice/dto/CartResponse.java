package com.vasav.ecom.orderservice.dto;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        List<CartItem> items
) {
    public record CartItem(
            UUID productId,
            int quantity
    ) {
    }
}
