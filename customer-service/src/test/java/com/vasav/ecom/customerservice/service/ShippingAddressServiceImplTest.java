package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.dto.CreateShippingAddressRequest;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.entity.ShippingAddress;
import com.vasav.ecom.customerservice.exception.BusinessRuleException;
import com.vasav.ecom.customerservice.exception.ResourceNotFoundException;
import com.vasav.ecom.customerservice.exception.UnauthorizedAccessException;
import com.vasav.ecom.customerservice.mapper.ShippingAddressMapper;
import com.vasav.ecom.customerservice.repository.CustomerRepository;
import com.vasav.ecom.customerservice.repository.ShippingAddressRepository;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.impl.ShippingAddressServiceImpl;
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
class ShippingAddressServiceImplTest {

    @Mock
    ShippingAddressRepository addressRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    CustomerService customerService;
    @Mock
    ShippingAddressMapper mapper;
    @InjectMocks
    ShippingAddressServiceImpl service;

    final CurrentUser user = new CurrentUser("kc-1", "jane", "jane@x.com", "Jane", null, "Doe");

    private static Customer customer(boolean active) {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setActive(active);
        return c;
    }

    private static CreateShippingAddressRequest request() {
        return new CreateShippingAddressRequest("J", "D", null, "1 Main", null, null,
                "City", null, "12345", "US", null, null);
    }

    @Test
    void listReturnsEmptyWhenCustomerNotProvisionedYet() {
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.empty());

        assertThat(service.getAddressesForCustomer(user)).isEmpty();
    }

    @Test
    void listRejectsDeactivatedCustomer() {
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.of(customer(false)));

        assertThatThrownBy(() -> service.getAddressesForCustomer(user))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void addProvisionsCustomerAndSavesAddress() {
        Customer c = customer(true);
        ShippingAddress entity = new ShippingAddress();
        when(customerService.getOrCreateCustomerEntity(user)).thenReturn(c);
        when(addressRepository.countByCustomerId(c.getId())).thenReturn(0L);
        when(mapper.toEntity(any())).thenReturn(entity);
        when(addressRepository.save(entity)).thenReturn(entity);

        service.addAddress(user, request());

        assertThat(entity.getCustomer()).isSameAs(c);
    }

    @Test
    void addRejectsWhenLimitReached() {
        Customer c = customer(true);
        when(customerService.getOrCreateCustomerEntity(user)).thenReturn(c);
        when(addressRepository.countByCustomerId(c.getId())).thenReturn(10L);

        assertThatThrownBy(() -> service.addAddress(user, request()))
                .isInstanceOf(BusinessRuleException.class);
        verify(addressRepository, never()).save(any());
    }

    @Test
    void cannotReadAnotherCustomersAddress() {
        Customer c = customer(true);
        UUID addressId = UUID.randomUUID();
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.of(c));
        when(addressRepository.findByIdAndCustomerId(addressId, c.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAddressById(user, addressId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteRemovesOwnedAddress() {
        Customer c = customer(true);
        ShippingAddress a = new ShippingAddress();
        UUID addressId = UUID.randomUUID();
        when(customerRepository.findByUserKeycloakUserId("kc-1")).thenReturn(Optional.of(c));
        when(addressRepository.findByIdAndCustomerId(addressId, c.getId())).thenReturn(Optional.of(a));

        service.deleteAddress(user, addressId);

        verify(addressRepository).delete(a);
    }
}
