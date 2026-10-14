package com.vasav.ecom.integrationservice.controller;

import com.vasav.ecom.integrationservice.dto.PayRequest;
import com.vasav.ecom.integrationservice.dto.PaymentResponse;
import com.vasav.ecom.integrationservice.security.CurrentUser;
import com.vasav.ecom.integrationservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.get(CurrentUser.from(jwt).subject(), orderId));
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId,
                                               @Valid @RequestBody PayRequest request) {
        return ResponseEntity.ok(paymentService.pay(CurrentUser.from(jwt).subject(), orderId, request));
    }
}
