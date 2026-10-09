package com.vasav.ecom.customerservice.controller;

import com.vasav.ecom.customerservice.dto.CreateShippingAddressRequest;
import com.vasav.ecom.customerservice.dto.ShippingAddressDto;
import com.vasav.ecom.customerservice.dto.UpdateShippingAddressRequest;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.ShippingAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/me/addresses")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<ShippingAddressDto>> getMyAddresses(@AuthenticationPrincipal Jwt jwt) {
        List<ShippingAddressDto> response = shippingAddressService.getAddressesForCustomer(CurrentUser.from(jwt));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ShippingAddressDto> getMyAddressById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId) {
        ShippingAddressDto response = shippingAddressService.getAddressById(CurrentUser.from(jwt), addressId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ShippingAddressDto> addAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateShippingAddressRequest request) {
        ShippingAddressDto response = shippingAddressService.addAddress(CurrentUser.from(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ShippingAddressDto> updateAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateShippingAddressRequest request) {
        ShippingAddressDto response = shippingAddressService.updateAddress(CurrentUser.from(jwt), addressId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID addressId) {
        shippingAddressService.deleteAddress(CurrentUser.from(jwt), addressId);
        return ResponseEntity.noContent().build();
    }
}
