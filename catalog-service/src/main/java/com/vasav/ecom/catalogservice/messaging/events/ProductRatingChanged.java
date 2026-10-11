package com.vasav.ecom.catalogservice.messaging.events;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRatingChanged(
        UUID productId,
        long ratingVersion,
        int ratingCount,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal averageRating
) {
}
