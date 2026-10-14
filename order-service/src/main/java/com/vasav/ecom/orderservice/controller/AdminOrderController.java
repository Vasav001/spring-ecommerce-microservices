package com.vasav.ecom.orderservice.controller;

import com.vasav.ecom.orderservice.dto.OrderResponse;
import com.vasav.ecom.orderservice.entity.enums.OrderStatus;
import com.vasav.ecom.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PagedModel<OrderResponse>> list(@RequestParam(required = false) OrderStatus status,
                                                          Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(orderService.listAll(status, pageable)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getAny(orderId));
    }
}
