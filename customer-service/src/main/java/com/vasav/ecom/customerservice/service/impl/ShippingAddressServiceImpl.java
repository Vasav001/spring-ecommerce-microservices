package com.vasav.ecom.customerservice.service.impl;

import com.vasav.ecom.customerservice.dto.CreateShippingAddressRequest;
import com.vasav.ecom.customerservice.dto.ShippingAddressDto;
import com.vasav.ecom.customerservice.dto.UpdateShippingAddressRequest;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.entity.ShippingAddress;
import com.vasav.ecom.customerservice.exception.BusinessRuleException;
import com.vasav.ecom.customerservice.exception.ResourceNotFoundException;
import com.vasav.ecom.customerservice.exception.UnauthorizedAccessException;
import com.vasav.ecom.customerservice.mapper.ShippingAddressMapper;
import com.vasav.ecom.customerservice.repository.CustomerRepository;
import com.vasav.ecom.customerservice.repository.ShippingAddressRepository;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.CustomerService;
import com.vasav.ecom.customerservice.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    static final int MAX_ADDRESSES_PER_CUSTOMER = 10;

    private final ShippingAddressRepository shippingAddressRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final ShippingAddressMapper shippingAddressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressDto> getAddressesForCustomer(CurrentUser currentUser) {
        return customerRepository.findByUserKeycloakUserId(currentUser.keycloakUserId())
                .map(customer -> {
                    assertActive(customer);
                    return shippingAddressMapper.toDtoList(shippingAddressRepository.findByCustomerId(customer.getId()));
                })
                .orElseGet(List::of);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingAddressDto getAddressById(CurrentUser currentUser, UUID addressId) {
        return shippingAddressMapper.toDto(findOwnedAddress(currentUser, addressId));
    }

    @Override
    public ShippingAddressDto addAddress(CurrentUser currentUser, CreateShippingAddressRequest request) {
        Customer customer = customerService.getOrCreateCustomerEntity(currentUser);
        if (shippingAddressRepository.countByCustomerId(customer.getId()) >= MAX_ADDRESSES_PER_CUSTOMER) {
            throw new BusinessRuleException("A maximum of " + MAX_ADDRESSES_PER_CUSTOMER + " addresses is allowed");
        }
        ShippingAddress address = shippingAddressMapper.toEntity(request);
        address.setCustomer(customer);
        return shippingAddressMapper.toDto(shippingAddressRepository.save(address));
    }

    @Override
    @Transactional
    public ShippingAddressDto updateAddress(CurrentUser currentUser, UUID addressId, UpdateShippingAddressRequest request) {
        ShippingAddress address = findOwnedAddress(currentUser, addressId);
        shippingAddressMapper.updateEntityFromRequest(request, address);
        return shippingAddressMapper.toDto(shippingAddressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(CurrentUser currentUser, UUID addressId) {
        shippingAddressRepository.delete(findOwnedAddress(currentUser, addressId));
    }

    private ShippingAddress findOwnedAddress(CurrentUser currentUser, UUID addressId) {
        Customer customer = customerRepository.findByUserKeycloakUserId(currentUser.keycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with ID: " + addressId));
        assertActive(customer);
        return shippingAddressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with ID: " + addressId));
    }

    private static void assertActive(Customer customer) {
        if (!customer.isActive()) {
            throw new UnauthorizedAccessException("Customer account is deactivated");
        }
    }
}
