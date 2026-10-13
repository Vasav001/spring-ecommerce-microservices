package com.vasav.ecom.inventoryservice.controller;

import com.vasav.ecom.inventoryservice.dto.AdjustStockRequest;
import com.vasav.ecom.inventoryservice.dto.StockItemResponse;
import com.vasav.ecom.inventoryservice.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final StockService stockService;

    @GetMapping
    public PagedModel<StockItemResponse> list(Pageable pageable) {
        return new PagedModel<>(stockService.list(pageable));
    }

    @GetMapping("/{productId}")
    public StockItemResponse get(@PathVariable UUID productId) {
        return stockService.get(productId);
    }

    @PostMapping("/{productId}/adjustments")
    public StockItemResponse adjust(@PathVariable UUID productId, @Valid @RequestBody AdjustStockRequest request) {
        return stockService.adjust(productId, request.delta());
    }
}
