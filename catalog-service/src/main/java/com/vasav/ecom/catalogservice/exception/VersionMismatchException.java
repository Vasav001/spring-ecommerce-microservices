package com.vasav.ecom.catalogservice.exception;

public class VersionMismatchException extends RuntimeException {
    public VersionMismatchException() {
        super("Resource was modified, reload it");
    }
}
