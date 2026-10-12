package com.vasav.ecom.catalogservice.exception;

import java.util.UUID;

public class AlreadyReviewedException extends RuntimeException {
    public AlreadyReviewedException(UUID productId) {
        super("You have already reviewed product " + productId);
    }
}
