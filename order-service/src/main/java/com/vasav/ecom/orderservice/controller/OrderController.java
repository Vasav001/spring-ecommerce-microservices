package com.vasav.ecom.orderservice.controller;

import com.vasav.ecom.orderservice.dto.OrderResponse;
import com.vasav.ecom.orderservice.security.CurrentUser;
import com.vasav.ecom.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
// my order/s
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PagedModel<OrderResponse>> list(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(orderService.list(CurrentUser.from(jwt).subject(), pageable)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.get(CurrentUser.from(jwt).subject(), orderId));
    }
}
