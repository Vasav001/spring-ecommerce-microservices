package com.vasav.ecom.customerservice.security;

import org.springframework.security.oauth2.jwt.Jwt;

public record CurrentUser(
        String keycloakUserId,
        String username,
        String email,
        String firstName,
        String middleName,
        String lastName
) {
    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("middle_name"),
                jwt.getClaimAsString("family_name")
        );
    }
}
