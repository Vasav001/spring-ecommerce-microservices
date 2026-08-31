package com.vasav.ecom.customerservice.service.impl;

import com.vasav.ecom.customerservice.dto.CreateShippingAddressRequest;
import com.vasav.ecom.customerservice.dto.ShippingAddressDto;
import com.vasav.ecom.customerservice.dto.UpdateShippingAddressRequest;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.entity.ShippingAddress;
import com.vasav.ecom.customerservice.exception.ResourceNotFoundException;
import com.vasav.ecom.customerservice.mapper.ShippingAddressMapper;
import com.vasav.ecom.customerservice.repository.CustomerRepository;
import com.vasav.ecom.customerservice.repository.ShippingAddressRepository;
import com.vasav.ecom.customerservice.service.ShippingAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;
    private final CustomerRepository customerRepository;
    private final ShippingAddressMapper shippingAddressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressDto> getAddressesForCustomer(String keycloakUserId) {
        Customer customer = getCustomerByKeycloakId(keycloakUserId);
        List<ShippingAddress> addresses = shippingAddressRepository.findByCustomerId(customer.getId());
        return shippingAddressMapper.toDtoList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingAddressDto getAddressById(String keycloakUserId, UUID addressId) {
        Customer customer = getCustomerByKeycloakId(keycloakUserId);
        ShippingAddress address = shippingAddressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with ID: " + addressId));
        return shippingAddressMapper.toDto(address);
    }

    @Override
    @Transactional
    public ShippingAddressDto addAddress(String keycloakUserId, CreateShippingAddressRequest request) {
        Customer customer = getCustomerByKeycloakId(keycloakUserId);
        ShippingAddress address = shippingAddressMapper.toEntity(request);
        address.setCustomer(customer);
        ShippingAddress savedAddress = shippingAddressRepository.save(address);
        return shippingAddressMapper.toDto(savedAddress);
    }

    @Override
    @Transactional
    public ShippingAddressDto updateAddress(String keycloakUserId, UUID addressId, UpdateShippingAddressRequest request) {
        Customer customer = getCustomerByKeycloakId(keycloakUserId);
        ShippingAddress address = shippingAddressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with ID: " + addressId));

        shippingAddressMapper.updateEntityFromRequest(request, address);
        ShippingAddress updatedAddress = shippingAddressRepository.save(address);
        return shippingAddressMapper.toDto(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String keycloakUserId, UUID addressId) {
        Customer customer = getCustomerByKeycloakId(keycloakUserId);
        ShippingAddress address = shippingAddressRepository.findByIdAndCustomerId(addressId, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found with ID: " + addressId));
        shippingAddressRepository.delete(address);
    }

    private Customer getCustomerByKeycloakId(String keycloakUserId) {
        return customerRepository.findByUserKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for keycloak ID: " + keycloakUserId));
    }
}
