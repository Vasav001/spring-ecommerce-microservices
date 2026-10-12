package com.vasav.ecom.catalogservice.config;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Keycloak-shaped JWTs for MockMvc. Authorities go through the production
 * {@link SecurityConfig#extractRoles} mapping (realm_access.roles -> ROLE_*), so role mapping is tested too.
 */
public final class TestJwts {

    public static final String ADMIN_SUB = "admin-1";

    private TestJwts() {
    }

    public static JwtRequestPostProcessor admin() {
        return user(ADMIN_SUB, "Ada", "Admin", "ADMIN");
    }

    public static JwtRequestPostProcessor customer(String subject) {
        return user(subject, "Jane", "Doe", "CUSTOMER");
    }

    public static JwtRequestPostProcessor user(String subject, String givenName, String familyName, String... roles) {
        return jwt()
                .jwt(j -> {
                    j.subject(subject).claim("realm_access", Map.of("roles", List.of(roles)));
                    if (givenName != null) {
                        j.claim("given_name", givenName);
                    }
                    if (familyName != null) {
                        j.claim("family_name", familyName);
                    }
                })
                .authorities(SecurityConfig::extractRoles);
    }
}
