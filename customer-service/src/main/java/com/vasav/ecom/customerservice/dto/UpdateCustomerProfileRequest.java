package com.vasav.ecom.customerservice.dto;

import com.vasav.ecom.customerservice.entity.enums.CommunicationPreference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record UpdateCustomerProfileRequest(
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
        String phone,

        List<@Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format") String> additionalPhones,

        @NotNull(message = "Communication preference is required")
        CommunicationPreference defaultCommunicationPreference
) {}
