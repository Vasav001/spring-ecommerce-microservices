package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.dto.AddressRequest;
import com.vasav.ecom.customerservice.dto.AddressResponse;
import com.vasav.ecom.customerservice.dto.ProfileRequest;
import com.vasav.ecom.customerservice.dto.ProfileResponse;
import com.vasav.ecom.customerservice.entity.Address;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.repository.AddressRepository;
import com.vasav.ecom.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    @Value("${customer.max-addresses:10}")
    private int maxAddresses;

    @Value("${images.url-prefix}")
    private String imageUrlPrefix;

    @Transactional
    public ProfileResponse get(String customerId) {
        return ProfileResponse.from(load(customerId), addressRepository.findByCustomerIdOrderByCreatedAt(customerId));
    }

    @Transactional
    public ProfileResponse update(String customerId, ProfileRequest request) {
        String url = request.profilePictureUrl();
        if (url != null && !url.startsWith(imageUrlPrefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "profilePictureUrl must start with " + imageUrlPrefix);
        }
        Customer customer = load(customerId);
        customer.setProfilePictureUrl(request.profilePictureUrl());
        customerRepository.saveAndFlush(customer);
        return ProfileResponse.from(customer, addressRepository.findByCustomerIdOrderByCreatedAt(customerId));
    }

    @Transactional
    public AddressResponse addAddress(String customerId, AddressRequest request) {
        load(customerId);
        if (addressRepository.countByCustomerId(customerId) >= maxAddresses) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "At most " + maxAddresses + " addresses allowed");
        }
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setCustomerId(customerId);
        request.applyTo(address);
        return AddressResponse.from(addressRepository.saveAndFlush(address));
    }

    @Transactional
    public AddressResponse updateAddress(String customerId, UUID addressId, AddressRequest request) {
        Address address = findAddress(customerId, addressId);
        request.applyTo(address);
        return AddressResponse.from(addressRepository.saveAndFlush(address));
    }

    @Transactional
    public void deleteAddress(String customerId, UUID addressId) {
        addressRepository.delete(findAddress(customerId, addressId));
    }

    private Customer load(String customerId) {
        customerRepository.createIfAbsent(customerId);
        return customerRepository.lockById(customerId).orElseThrow();
    }

    private Address findAddress(String customerId, UUID addressId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }
}
