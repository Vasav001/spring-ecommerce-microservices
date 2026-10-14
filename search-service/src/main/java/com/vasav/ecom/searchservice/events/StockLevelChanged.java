package com.vasav.ecom.searchservice.events;

import java.util.UUID;

public record StockLevelChanged(
        UUID productId,
        boolean inStock
) {
}
