package com.vasav.ecom.customerservice.controller;

import com.vasav.ecom.customerservice.dto.AddressRequest;
import com.vasav.ecom.customerservice.dto.AddressResponse;
import com.vasav.ecom.customerservice.dto.ProfileRequest;
import com.vasav.ecom.customerservice.dto.ProfileResponse;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/me")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ProfileResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(customerService.get(CurrentUser.from(jwt).subject()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(customerService.update(CurrentUser.from(jwt).subject(), request));
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerService.addAddress(CurrentUser.from(jwt).subject(), request));
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId,
                                                         @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(customerService.updateAddress(CurrentUser.from(jwt).subject(), addressId, request));
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID addressId) {
        customerService.deleteAddress(CurrentUser.from(jwt).subject(), addressId);
        return ResponseEntity.noContent().build();
    }
}
