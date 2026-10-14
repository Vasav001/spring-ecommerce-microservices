package com.vasav.ecom.customerservice.dto;

import com.vasav.ecom.customerservice.entity.Address;
import com.vasav.ecom.customerservice.entity.Customer;

import java.util.List;

public record ProfileResponse(
        String id,
        String profilePictureUrl,
        List<AddressResponse> addresses
) {
    public static ProfileResponse from(Customer customer, List<Address> addresses) {
        return new ProfileResponse(customer.getId(), customer.getProfilePictureUrl(),
                addresses.stream().map(AddressResponse::from).toList());
    }
}
