package com.vasav.ecom.customerservice.controller;

import com.vasav.ecom.customerservice.dto.CustomerResponseDto;
import com.vasav.ecom.customerservice.dto.UpdateCustomerProfileRequest;
import com.vasav.ecom.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CustomerResponseDto> getCurrentCustomer(@AuthenticationPrincipal Jwt jwt) {
        CustomerResponseDto response = customerService.getOrCreateCurrentCustomer(jwt);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CustomerResponseDto> updateCurrentCustomerProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateCustomerProfileRequest request) {
        CustomerResponseDto response = customerService.updateCustomerProfile(jwt.getSubject(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable UUID id) {
        CustomerResponseDto response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerResponseDto>> getAllCustomers(@PageableDefault(size = 20) Pageable pageable) {
        Page<CustomerResponseDto> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setCustomerActiveStatus(
            @PathVariable UUID id,
            @RequestParam boolean active) {
        customerService.setCustomerActiveStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}
