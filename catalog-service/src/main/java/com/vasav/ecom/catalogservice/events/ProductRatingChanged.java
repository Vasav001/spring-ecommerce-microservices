package com.vasav.ecom.catalogservice.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRatingChanged(
        UUID productId,
        BigDecimal averageRating,
        long reviewCount
) {
}
