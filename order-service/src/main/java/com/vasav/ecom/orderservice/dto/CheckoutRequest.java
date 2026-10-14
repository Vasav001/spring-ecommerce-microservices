package com.vasav.ecom.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull @Valid ShippingAddress shippingAddress
) {
}
