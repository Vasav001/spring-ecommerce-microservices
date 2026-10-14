package com.vasav.ecom.inventoryservice.controller;

import com.vasav.ecom.inventoryservice.dto.StockAvailabilityResponse;
import com.vasav.ecom.inventoryservice.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private static final int MAX_PRODUCTS = 100;

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockAvailabilityResponse>> availability(@RequestParam List<UUID> productIds) {
        if (productIds.isEmpty() || productIds.size() > MAX_PRODUCTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productIds must have 1 to " + MAX_PRODUCTS + " ids");
        }
        return ResponseEntity.ok(stockService.availability(productIds));
    }
}
