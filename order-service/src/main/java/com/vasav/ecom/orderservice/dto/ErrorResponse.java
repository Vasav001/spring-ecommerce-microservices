package com.vasav.ecom.orderservice.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Instant timestamp,
        Map<String, String> validationErrors
) {
    public ErrorResponse(int status, String message, Instant timestamp) {
        this(status, message, timestamp, null);
    }
}
