package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.dto.UpdateCustomerProfileRequest;
import com.vasav.ecom.customerservice.entity.AppUser;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.entity.enums.CommunicationPreference;
import com.vasav.ecom.customerservice.exception.BusinessRuleException;
import com.vasav.ecom.customerservice.exception.ResourceNotFoundException;
import com.vasav.ecom.customerservice.exception.UnauthorizedAccessException;
import com.vasav.ecom.customerservice.mapper.CustomerMapper;
import com.vasav.ecom.customerservice.repository.CustomerRepository;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    AppUserService appUserService;
    @Mock
    CustomerMapper customerMapper;
    @Mock
    TransactionTemplate transactionTemplate;

    CustomerServiceImpl service;

    final CurrentUser user = new CurrentUser("kc-1", "jane", "jane@x.com", "Jane", null, "Doe");

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(customerRepository, appUserService, customerMapper, transactionTemplate);
    }

    @SuppressWarnings("unchecked")
    private void runCallbacksDirectly() {
        when(transactionTemplate.execute(any())).thenAnswer(i ->
                ((TransactionCallback<Object>) i.getArgument(0)).doInTransaction(null));
    }

    private static AppUser appUser() {
        AppUser u = new AppUser();
        u.setKeycloakUserId("kc-1");
        return u;
    }

    private static Customer customer(boolean active) {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setActive(active);
        return c;
    }

    @Test
    void createsCustomerOnFirstCall() {
        runCallbacksDirectly();
        when(appUserService.syncWithKeycloak(user)).thenReturn(appUser());
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

        Customer result = service.getOrCreateCustomerEntity(user);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getDefaultCommunicationPreference()).isEqualTo(CommunicationPreference.EMAIL);
    }

    @Test
    void rejectsDeactivatedCustomer() {
        runCallbacksDirectly();
        when(appUserService.syncWithKeycloak(user)).thenReturn(appUser());
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.of(customer(false)));

        assertThatThrownBy(() -> service.getOrCreateCustomerEntity(user))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void retriesOnceOnConcurrentInsert() {
        Customer winner = customer(true);
        when(transactionTemplate.execute(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"))
                .thenReturn(winner);

        assertThat(service.getOrCreateCustomerEntity(user)).isSameAs(winner);
    }

    @Test
    void profileUpdateRequiresPhoneForSms() {
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.of(customer(true)));

        assertThatThrownBy(() -> service.updateCustomerProfile(user,
                new UpdateCustomerProfileRequest(null, null, CommunicationPreference.SMS)))
                .isInstanceOf(BusinessRuleException.class);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void profileUpdateWithoutCustomerIsNotFound() {
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCustomerProfile(user,
                new UpdateCustomerProfileRequest(null, null, CommunicationPreference.EMAIL)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listRejectsUnknownSortField() {
        assertThatThrownBy(() -> service.getAllCustomers(
                org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("bogus"))))
                .isInstanceOf(com.vasav.ecom.customerservice.exception.InvalidRequestException.class);
    }

    @Test
    void adminCanDeactivateCustomer() {
        Customer c = customer(true);
        when(customerRepository.findById(c.getId())).thenReturn(Optional.of(c));

        service.setCustomerActiveStatus(c.getId(), false);

        assertThat(c.isActive()).isFalse();
        verify(customerRepository).save(c);
    }
}
