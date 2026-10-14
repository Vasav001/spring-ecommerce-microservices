package com.vasav.ecom.catalogservice.events;

import java.util.List;
import java.util.UUID;

public record OrderEvent(
        UUID orderId,
        String customerId,
        List<Item> items
) {
    public record Item(
            UUID productId
    ) {
    }
}
