package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.entity.AppUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public interface AppUserService {
    AppUser syncWithKeycloak(Jwt jwt);

    AppUser getByKeycloakUserId(String keycloakUserId);

    AppUser getById(UUID id);
    //TODO CHECK IF LATER UNUSED FOR CLEANUP
}
