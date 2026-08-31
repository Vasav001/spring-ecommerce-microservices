package com.vasav.ecom.customerservice.service.impl;

import com.vasav.ecom.customerservice.entity.AppUser;
import com.vasav.ecom.customerservice.exception.ResourceNotFoundException;
import com.vasav.ecom.customerservice.repository.AppUserRepository;
import com.vasav.ecom.customerservice.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public AppUser syncWithKeycloak(Jwt jwt) {
        String keycloakUserId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            username = keycloakUserId;
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = username + "@local.dev";
        }

        String firstName = jwt.getClaimAsString("given_name");
        if (firstName == null || firstName.isBlank()) {
            firstName = username;
        }

        String lastName = jwt.getClaimAsString("family_name");
        if (lastName == null || lastName.isBlank()) {
            lastName = username;
        }

        String middleName = jwt.getClaimAsString("middle_name");

        final String finalUsername = username;
        final String finalEmail = email;
        final String finalFirstName = firstName;
        final String finalLastName = lastName;
        final String finalMiddleName = middleName;

        return appUserRepository.findByKeycloakUserId(keycloakUserId)
                .map(existingUser -> {
                    boolean modified = false;
                    if (!Objects.equals(existingUser.getUsername(), finalUsername)) {
                        existingUser.setUsername(finalUsername);
                        modified = true;
                    }
                    if (!Objects.equals(existingUser.getEmail(), finalEmail)) {
                        existingUser.setEmail(finalEmail);
                        modified = true;
                    }
                    if (!Objects.equals(existingUser.getFirstName(), finalFirstName)) {
                        existingUser.setFirstName(finalFirstName);
                        modified = true;
                    }
                    if (!Objects.equals(existingUser.getLastName(), finalLastName)) {
                        existingUser.setLastName(finalLastName);
                        modified = true;
                    }
                    if (!Objects.equals(existingUser.getMiddleName(), finalMiddleName)) {
                        existingUser.setMiddleName(finalMiddleName);
                        modified = true;
                    }
                    return modified ? appUserRepository.save(existingUser) : existingUser;
                })
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setKeycloakUserId(keycloakUserId);
                    newUser.setUsername(finalUsername);
                    newUser.setEmail(finalEmail);
                    newUser.setFirstName(finalFirstName);
                    newUser.setLastName(finalLastName);
                    newUser.setMiddleName(finalMiddleName);
                    return appUserRepository.save(newUser);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser getByKeycloakUserId(String keycloakUserId) {
        return appUserRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser getById(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }
}
