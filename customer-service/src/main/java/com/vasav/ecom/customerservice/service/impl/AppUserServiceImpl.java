package com.vasav.ecom.customerservice.service.impl;

import com.vasav.ecom.customerservice.entity.AppUser;
import com.vasav.ecom.customerservice.exception.DuplicateResourceException;
import com.vasav.ecom.customerservice.repository.AppUserRepository;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public AppUser syncWithKeycloak(CurrentUser currentUser) {
        String keycloakUserId = currentUser.keycloakUserId();
        String username = orDefault(currentUser.username(), keycloakUserId);
        String email = orDefault(currentUser.email(), username + "@local.dev");
        String firstName = orDefault(currentUser.firstName(), username);
        String lastName = orDefault(currentUser.lastName(), username);
        String middleName = currentUser.middleName();

        AppUser user = appUserRepository.findByKeycloakUserId(keycloakUserId).orElse(null);

        if (user == null) {
            assertUnique(username, email, null);
            user = new AppUser();
            user.setKeycloakUserId(keycloakUserId);
            apply(user, username, email, firstName, middleName, lastName);
            return appUserRepository.save(user);
        }

        boolean modified = !Objects.equals(user.getUsername(), username)
                || !Objects.equals(user.getEmail(), email)
                || !Objects.equals(user.getFirstName(), firstName)
                || !Objects.equals(user.getMiddleName(), middleName)
                || !Objects.equals(user.getLastName(), lastName);
        if (!modified) {
            return user;
        }
        assertUnique(username, email, user);
        apply(user, username, email, firstName, middleName, lastName);
        return appUserRepository.save(user);
    }

    private void assertUnique(String username, String email, AppUser self) {
        appUserRepository.findByUsername(username)
                .filter(other -> self == null || !other.getId().equals(self.getId()))
                .ifPresent(other -> {
                    throw new DuplicateResourceException("Username already in use: " + username);
                });
        appUserRepository.findByEmail(email)
                .filter(other -> self == null || !other.getId().equals(self.getId()))
                .ifPresent(other -> {
                    throw new DuplicateResourceException("Email already in use: " + email);
                });
    }

    private static void apply(AppUser user, String username, String email, String firstName,
                              String middleName, String lastName) {
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
