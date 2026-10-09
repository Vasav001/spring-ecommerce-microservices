package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.dto.CustomerResponseDto;
import com.vasav.ecom.customerservice.dto.UpdateCustomerProfileRequest;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    CustomerResponseDto getOrCreateCurrentCustomer(CurrentUser currentUser);

    Customer getOrCreateCustomerEntity(CurrentUser currentUser);

    CustomerResponseDto getCustomerById(UUID customerId);

    CustomerResponseDto updateCustomerProfile(CurrentUser currentUser, UpdateCustomerProfileRequest request);

    Page<CustomerResponseDto> getAllCustomers(Pageable pageable);

    void setCustomerActiveStatus(UUID customerId, boolean active);
}
