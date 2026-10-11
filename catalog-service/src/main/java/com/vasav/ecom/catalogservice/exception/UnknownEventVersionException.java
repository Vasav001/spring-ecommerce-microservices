package com.vasav.ecom.catalogservice.exception;

public class UnknownEventVersionException extends RuntimeException {

    public UnknownEventVersionException(String eventType, int eventVersion) {
        super("Unsupported event version: " + eventType + " v" + eventVersion);
    }
}