package com.vasav.ecom.orderservice.controller;

import com.vasav.ecom.orderservice.dto.CartItemRequest;
import com.vasav.ecom.orderservice.dto.CartResponse;
import com.vasav.ecom.orderservice.security.CurrentUser;
import com.vasav.ecom.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    //    cart = volatile(redis). methods:
//  cart -> get/delete,
//  cart product -> update(qty)/delete
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> get(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cartService.get(CurrentUser.from(jwt).subject()));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> setQuantity(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId,
                                                    @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.setQuantity(CurrentUser.from(jwt).subject(), productId, request.quantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId) {
        return ResponseEntity.ok(cartService.remove(CurrentUser.from(jwt).subject(), productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal Jwt jwt) {
        cartService.clear(CurrentUser.from(jwt).subject());
        return ResponseEntity.noContent().build();
    }
}
