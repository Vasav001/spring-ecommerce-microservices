package com.vasav.ecom.orderservice.dto;

import com.vasav.ecom.orderservice.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShippingAddress(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 32) String phone,
        @NotBlank @Size(max = 255) String line1,
        @Size(max = 255) String line2,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 20) String postalCode,
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "must be a 2-letter ISO country code") String country
) {
    public static ShippingAddress from(Order order) {
        return new ShippingAddress(order.getShipName(), order.getShipPhone(), order.getShipLine1(),
                order.getShipLine2(), order.getShipCity(), order.getShipState(), order.getShipPostalCode(),
                order.getShipCountry());
    }

    public void applyTo(Order order) {
        order.setShipName(name);
        order.setShipPhone(phone);
        order.setShipLine1(line1);
        order.setShipLine2(line2);
        order.setShipCity(city);
        order.setShipState(state);
        order.setShipPostalCode(postalCode);
        order.setShipCountry(country);
    }
}
