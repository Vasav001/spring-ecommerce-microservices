package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.dto.CreateShippingAddressRequest;
import com.vasav.ecom.customerservice.dto.ShippingAddressDto;
import com.vasav.ecom.customerservice.dto.UpdateShippingAddressRequest;
import com.vasav.ecom.customerservice.security.CurrentUser;

import java.util.List;
import java.util.UUID;

public interface ShippingAddressService {
    List<ShippingAddressDto> getAddressesForCustomer(CurrentUser currentUser);

    ShippingAddressDto getAddressById(CurrentUser currentUser, UUID addressId);

    ShippingAddressDto addAddress(CurrentUser currentUser, CreateShippingAddressRequest request);

    ShippingAddressDto updateAddress(CurrentUser currentUser, UUID addressId, UpdateShippingAddressRequest request);

    void deleteAddress(CurrentUser currentUser, UUID addressId);
}
