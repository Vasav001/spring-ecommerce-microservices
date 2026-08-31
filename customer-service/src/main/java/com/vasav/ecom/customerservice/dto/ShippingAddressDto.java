package com.vasav.ecom.customerservice.dto;

import java.time.Instant;
import java.util.UUID;

public record ShippingAddressDto(
        UUID id,
        String firstName,
        String lastName,
        String companyName,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String city,
        String state,
        String postalCode,
        String country,
        String phone,
        String email,
        Instant createdAt,
        Instant updatedAt
) {}
