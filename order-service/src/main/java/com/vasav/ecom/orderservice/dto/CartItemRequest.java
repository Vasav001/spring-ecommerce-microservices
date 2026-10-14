package com.vasav.ecom.orderservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull @Min(0) @Max(99) Integer quantity
) {
}
