package com.vasav.ecom.catalogservice.exception;

public class NotReviewAuthorException extends RuntimeException {
    public NotReviewAuthorException() {
        super("Only the author can change this review");
    }
}
