package com.vasav.ecom.catalogservice.exception;

import java.util.UUID;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(UUID reviewId) {
        super("Review not found: " + reviewId);
    }
}
