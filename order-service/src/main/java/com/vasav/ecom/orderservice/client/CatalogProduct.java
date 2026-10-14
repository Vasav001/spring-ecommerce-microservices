package com.vasav.ecom.orderservice.client;

import java.math.BigDecimal;
import java.util.UUID;

public record CatalogProduct(
        UUID productId,
        String name,
        BigDecimal price,
        boolean active
) {
}
