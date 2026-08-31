package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.dto.CreateShippingAddressRequest;
import com.vasav.ecom.customerservice.dto.ShippingAddressDto;
import com.vasav.ecom.customerservice.dto.UpdateShippingAddressRequest;

import java.util.List;
import java.util.UUID;

public interface ShippingAddressService {
    List<ShippingAddressDto> getAddressesForCustomer(String keycloakUserId);

    ShippingAddressDto getAddressById(String keycloakUserId, UUID addressId);

    ShippingAddressDto addAddress(String keycloakUserId, CreateShippingAddressRequest request);

    ShippingAddressDto updateAddress(String keycloakUserId, UUID addressId, UpdateShippingAddressRequest request);

    void deleteAddress(String keycloakUserId, UUID addressId);
}
