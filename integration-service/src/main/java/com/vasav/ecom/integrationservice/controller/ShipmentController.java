package com.vasav.ecom.integrationservice.controller;

import com.vasav.ecom.integrationservice.dto.ShipmentResponse;
import com.vasav.ecom.integrationservice.security.CurrentUser;
import com.vasav.ecom.integrationservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ShipmentResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.get(CurrentUser.from(jwt).subject(), orderId));
    }
}
