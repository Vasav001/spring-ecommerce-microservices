package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.dto.CustomerResponseDto;
import com.vasav.ecom.customerservice.dto.UpdateCustomerProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public interface CustomerService {
    CustomerResponseDto getOrCreateCurrentCustomer(Jwt jwt);

    CustomerResponseDto getCustomerById(UUID customerId);

    CustomerResponseDto getCustomerByKeycloakId(String keycloakUserId);

    CustomerResponseDto updateCustomerProfile(String keycloakUserId, UpdateCustomerProfileRequest request);

    Page<CustomerResponseDto> getAllCustomers(Pageable pageable);

    void setCustomerActiveStatus(UUID customerId, boolean active);
}
