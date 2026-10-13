package com.vasav.ecom.inventoryservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        @NotNull Integer delta
) {
    @AssertTrue(message = "delta must not be zero")
    public boolean isNonZero() {
        return delta == null || delta != 0;
    }
}
