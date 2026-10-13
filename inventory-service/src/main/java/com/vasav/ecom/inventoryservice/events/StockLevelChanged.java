package com.vasav.ecom.inventoryservice.events;

import java.util.UUID;

public record StockLevelChanged(
        UUID productId,
        boolean inStock
        // rather than exact number, we send boolean on stock flip i.e
        // 0 then false and positive like 1 or 20 then true
) {
}
