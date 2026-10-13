package com.vasav.ecom.inventoryservice.events;

import java.util.List;
import java.util.UUID;

public record ReserveInventory(
        UUID orderId,
        List<Item> items
) {
    public record Item(
            UUID productId,
            int quantity
    ) {
    }
}
