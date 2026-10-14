package com.vasav.ecom.orderservice.dto;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        List<Item> items
) {
    public record Item(
            UUID productId,
            int quantity
    ) {
    }
}
