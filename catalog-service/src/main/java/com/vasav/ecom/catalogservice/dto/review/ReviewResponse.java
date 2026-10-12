package com.vasav.ecom.catalogservice.dto.review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        String authorName,
        short rating,
        String comment,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}