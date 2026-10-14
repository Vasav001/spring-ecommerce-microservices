package com.vasav.ecom.orderservice.controller;

import com.vasav.ecom.orderservice.dto.CheckoutRequest;
import com.vasav.ecom.orderservice.dto.OrderResponse;
import com.vasav.ecom.orderservice.security.CurrentUser;
import com.vasav.ecom.orderservice.service.CartService;
import com.vasav.ecom.orderservice.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {
//  saga
    private static final int MAX_KEY_LENGTH = 64;

    private final CheckoutService checkoutService;
    private final CartService cartService;

    @PostMapping
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                  @Valid @RequestBody CheckoutRequest request) {
        if (idempotencyKey.isBlank() || idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key must be 1 to " + MAX_KEY_LENGTH + " characters");
        }
        String customerId = CurrentUser.from(jwt).subject();
        CheckoutService.Result result;
        try {
            result = checkoutService.checkout(customerId, idempotencyKey, request.shippingAddress());
        } catch (DataIntegrityViolationException e) {
            result = checkoutService.findByIdempotencyKey(customerId, idempotencyKey)
                    .map(order -> new CheckoutService.Result(order, false))
                    .orElseThrow(() -> e);
        }
        if (result.created()) {
            cartService.clear(customerId);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result.order());
    }
}
