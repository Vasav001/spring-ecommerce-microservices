package com.vasav.ecom.integrationservice.controller;

import com.vasav.ecom.integrationservice.dto.ShipmentResponse;
import com.vasav.ecom.integrationservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/shipments")
@RequiredArgsConstructor
public class AdminShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ShipmentResponse> get(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.getAny(orderId));
    }

    @PostMapping("/{orderId}/dispatch")
    public ResponseEntity<ShipmentResponse> dispatch(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.dispatch(orderId));
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<ShipmentResponse> deliver(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.deliver(orderId));
    }

    @PostMapping("/{orderId}/fail")
    public ResponseEntity<ShipmentResponse> fail(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.fail(orderId));
    }
}
