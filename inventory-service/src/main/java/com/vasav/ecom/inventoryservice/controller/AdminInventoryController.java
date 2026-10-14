package com.vasav.ecom.inventoryservice.controller;

import com.vasav.ecom.inventoryservice.dto.AdjustStockRequest;
import com.vasav.ecom.inventoryservice.dto.StockItemResponse;
import com.vasav.ecom.inventoryservice.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<PagedModel<StockItemResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(stockService.list(pageable)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<StockItemResponse> get(@PathVariable UUID productId) {
        return ResponseEntity.ok(stockService.get(productId));
    }

    @PostMapping("/{productId}/adjustments")
    public ResponseEntity<StockItemResponse> adjust(@PathVariable UUID productId,
                                                    @Valid @RequestBody AdjustStockRequest request) {
        return ResponseEntity.ok(stockService.adjust(productId, request.delta()));
    }
}
