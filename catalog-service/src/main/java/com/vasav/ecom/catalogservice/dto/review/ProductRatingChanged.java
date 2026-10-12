package com.vasav.ecom.catalogservice.dto.review;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRatingChanged(
        UUID productId,
        long ratingVersion,
        int ratingCount,
        BigDecimal averageRating
) {
}