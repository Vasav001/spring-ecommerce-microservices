package com.vasav.ecom.customerservice.dto;

import java.time.Instant;
import java.util.UUID;

public record AppUserDto(
        UUID id,
        String username,
        String email,
        String firstName,
        String middleName,
        String lastName,
        String keycloakUserId,
        Instant createdAt,
        Instant updatedAt
) {}
