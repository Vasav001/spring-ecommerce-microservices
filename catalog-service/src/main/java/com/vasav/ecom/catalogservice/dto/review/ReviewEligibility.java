package com.vasav.ecom.catalogservice.dto.review;

public record ReviewEligibility(
        boolean canReview,
        String reason
) {
}