package com.vasav.ecom.searchservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpserted(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String status
) {
}
