package com.vasav.ecom.customerservice.service.impl;

import com.vasav.ecom.customerservice.dto.CustomerResponseDto;
import com.vasav.ecom.customerservice.dto.UpdateCustomerProfileRequest;
import com.vasav.ecom.customerservice.entity.AppUser;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.entity.enums.CommunicationPreference;
import com.vasav.ecom.customerservice.exception.ResourceNotFoundException;
import com.vasav.ecom.customerservice.mapper.CustomerMapper;
import com.vasav.ecom.customerservice.repository.CustomerRepository;
import com.vasav.ecom.customerservice.service.AppUserService;
import com.vasav.ecom.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserService appUserService;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponseDto getOrCreateCurrentCustomer(Jwt jwt) {
        AppUser appUser = appUserService.syncWithKeycloak(jwt);
        Customer customer = customerRepository.findByUserKeycloakUserId(appUser.getKeycloakUserId())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setUser(appUser);
                    newCustomer.setDefaultCommunicationPreference(CommunicationPreference.EMAIL);
                    newCustomer.setActive(true);
                    return customerRepository.save(newCustomer);
                });
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findByIdWithUser(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerByKeycloakId(String keycloakUserId) {
        Customer customer = customerRepository.findByUserKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with keycloak ID: " + keycloakUserId));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerResponseDto updateCustomerProfile(String keycloakUserId, UpdateCustomerProfileRequest request) {
        Customer customer = customerRepository.findByUserKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with keycloak ID: " + keycloakUserId));

        customerMapper.updateEntityFromRequest(request, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toDto(updatedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> getAllCustomers(Pageable pageable) {
        return customerRepository.findAllWithUser(pageable)
                .map(customerMapper::toDto);
    }

    @Override
    @Transactional
    public void setCustomerActiveStatus(UUID customerId, boolean active) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        customer.setActive(active);
        customerRepository.save(customer);
    }
}
