package com.vasav.ecom.customerservice.security;

import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

public record CurrentUser(
        String subject,
        String email,
        String firstName,
        String lastName
) {
    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                jwt.getClaimAsString(JwtClaimNames.SUB),
                jwt.getClaimAsString(StandardClaimNames.EMAIL),
                jwt.getClaimAsString(StandardClaimNames.GIVEN_NAME),
                jwt.getClaimAsString(StandardClaimNames.FAMILY_NAME)
        );
    }

    public String displayName() {
        if (firstName != null && !firstName.isBlank()) {
            return lastName == null || lastName.isBlank()
                    ? firstName
                    : firstName + " " + lastName.charAt(0) + ".";
        }
        return "Customer";
    }
}
