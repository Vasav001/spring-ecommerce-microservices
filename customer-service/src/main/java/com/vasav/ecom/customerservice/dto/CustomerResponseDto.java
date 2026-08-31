package com.vasav.ecom.customerservice.dto;

import com.vasav.ecom.customerservice.entity.enums.CommunicationPreference;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomerResponseDto(
        UUID id,
        AppUserDto user,
        String phone,
        List<String> additionalPhones,
        CommunicationPreference defaultCommunicationPreference,
        boolean active,
        List<ShippingAddressDto> shippingAddresses,
        Instant createdAt,
        Instant updatedAt
) {}
