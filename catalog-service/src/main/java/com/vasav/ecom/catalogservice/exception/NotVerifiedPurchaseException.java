package com.vasav.ecom.catalogservice.exception;

import java.util.UUID;

public class NotVerifiedPurchaseException extends RuntimeException {
    public NotVerifiedPurchaseException(UUID productId) {
        super("Only verified purchasers can review product " + productId);
    }
}
