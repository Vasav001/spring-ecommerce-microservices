package com.vasav.ecom.customerservice.dto;

import com.vasav.ecom.customerservice.entity.Address;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String name,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(address.getId(), address.getName(), address.getPhone(), address.getLine1(),
                address.getLine2(), address.getCity(), address.getState(), address.getPostalCode(),
                address.getCountry());
    }
}
