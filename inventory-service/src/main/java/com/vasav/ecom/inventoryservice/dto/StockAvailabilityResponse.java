package com.vasav.ecom.inventoryservice.dto;

import java.util.UUID;

public record StockAvailabilityResponse(
        UUID productId,
        int available,
        boolean inStock
) {
}
