package com.vasav.ecom.catalogservice.events;

import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpserted(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        ProductStatus status
) {
}
