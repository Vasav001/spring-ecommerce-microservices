package com.vasav.ecom.integrationservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record PayRequest(
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        Boolean fail
) {
    public boolean failed() {
        return Boolean.TRUE.equals(fail);
    }

    @AssertTrue(message = "amount is required unless fail is true")
    public boolean isAmountPresent() {
        return failed() || amount != null;
    }
}
