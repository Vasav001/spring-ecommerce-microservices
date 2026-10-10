package com.vasav.ecom.customerservice.controller;

import com.vasav.ecom.customerservice.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser currentUser = CurrentUser.from(jwt);
        return Map.of(
                "message", "Hello " + currentUser.firstName(),
                "currentUser", currentUser
        );
    }

    @GetMapping("/jwt")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> jwt(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return Map.of(
                "claims", jwt.getClaims(),
                "authorities", authorities
        );
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, Object> customerOnly(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("message", "You have the CUSTOMER role", "subject", CurrentUser.from(jwt).subject());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminOnly(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("message", "You have the ADMIN role", "subject", CurrentUser.from(jwt).subject());
    }
}
