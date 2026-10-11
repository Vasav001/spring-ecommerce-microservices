package com.vasav.ecom.catalogservice.messaging.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpserted(
        UUID productId,
        long version,
        String sku,
        String name,
        String description,
        String brand,
        String category,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal price,
        String status
) {
}
