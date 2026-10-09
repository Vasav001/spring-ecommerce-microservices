package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.entity.AppUser;
import com.vasav.ecom.customerservice.exception.DuplicateResourceException;
import com.vasav.ecom.customerservice.repository.AppUserRepository;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.impl.AppUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    AppUserRepository repository;
    @InjectMocks
    AppUserServiceImpl service;

    private static AppUser existing(String keycloakId) {
        AppUser u = new AppUser();
        u.setId(UUID.randomUUID());
        u.setKeycloakUserId(keycloakId);
        u.setUsername("jane");
        u.setEmail("jane@x.com");
        u.setFirstName("Jane");
        u.setLastName("Doe");
        return u;
    }

    @Test
    void createsUserWithFallbacksWhenClaimsMissing() {
        when(repository.findByKeycloakUserId("kc-1")).thenReturn(Optional.empty());
        when(repository.findByUsername("kc-1")).thenReturn(Optional.empty());
        when(repository.findByEmail("kc-1@local.dev")).thenReturn(Optional.empty());
        when(repository.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));

        AppUser saved = service.syncWithKeycloak(new CurrentUser("kc-1", null, null, null, null, null));

        assertThat(saved.getUsername()).isEqualTo("kc-1");
        assertThat(saved.getEmail()).isEqualTo("kc-1@local.dev");
        assertThat(saved.getFirstName()).isEqualTo("kc-1");
        assertThat(saved.getLastName()).isEqualTo("kc-1");
    }

    @Test
    void doesNotSaveWhenNothingChanged() {
        AppUser user = existing("kc-1");
        when(repository.findByKeycloakUserId("kc-1")).thenReturn(Optional.of(user));

        service.syncWithKeycloak(new CurrentUser("kc-1", "jane", "jane@x.com", "Jane", null, "Doe"));

        verify(repository, never()).save(any());
    }

    @Test
    void updatesChangedFields() {
        AppUser user = existing("kc-1");
        when(repository.findByKeycloakUserId("kc-1")).thenReturn(Optional.of(user));
        when(repository.findByUsername("jane")).thenReturn(Optional.of(user));
        when(repository.findByEmail("new@x.com")).thenReturn(Optional.empty());
        when(repository.save(user)).thenReturn(user);

        AppUser result = service.syncWithKeycloak(new CurrentUser("kc-1", "jane", "new@x.com", "Jane", null, "Doe"));

        assertThat(result.getEmail()).isEqualTo("new@x.com");
    }

    @Test
    void rejectsEmailUsedByAnotherUser() {
        AppUser other = existing("kc-other");
        when(repository.findByKeycloakUserId("kc-1")).thenReturn(Optional.empty());
        when(repository.findByUsername("jane2")).thenReturn(Optional.empty());
        when(repository.findByEmail("jane@x.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.syncWithKeycloak(
                new CurrentUser("kc-1", "jane2", "jane@x.com", "Jane", null, "Doe")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }
}
