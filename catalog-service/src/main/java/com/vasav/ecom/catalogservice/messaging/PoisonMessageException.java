package com.vasav.ecom.catalogservice.messaging;

public class PoisonMessageException extends RuntimeException {

    public PoisonMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
