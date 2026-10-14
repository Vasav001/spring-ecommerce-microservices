package com.vasav.ecom.catalogservice.dto;

import com.vasav.ecom.catalogservice.entity.Review;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        String authorName,
        int rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(review.getId(), review.getProductId(), review.getAuthorName(), review.getRating(),
                review.getComment(), review.getCreatedAt(), review.getUpdatedAt());
    }
}
