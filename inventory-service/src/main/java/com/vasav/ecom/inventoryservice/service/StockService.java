package com.vasav.ecom.inventoryservice.service;

import com.vasav.ecom.inventoryservice.dto.StockAvailabilityResponse;
import com.vasav.ecom.inventoryservice.dto.StockItemResponse;
import com.vasav.ecom.inventoryservice.entity.StockItem;
import com.vasav.ecom.inventoryservice.producer.StockLevelProducer;
import com.vasav.ecom.inventoryservice.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {
    // get all/one stock item, update item stock (+outbox), get availability of requested products

    private final StockItemRepository stockItemRepository;
    private final StockLevelProducer stockLevelProducer;

    //    get all stock items
    @Transactional(readOnly = true)
    public Page<StockItemResponse> list(Pageable pageable) {
        return stockItemRepository.findAll(pageable).map(StockItemResponse::from);
    }

    //    get stock item
    @Transactional(readOnly = true)
    public StockItemResponse get(UUID productId) {
        return StockItemResponse.from(find(productId));
    }

    //    check availability for each item requested
    @Transactional(readOnly = true)
    public List<StockAvailabilityResponse> availability(List<UUID> productIds) {
        Map<UUID, Integer> available = stockItemRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(StockItem::getProductId, StockItem::available));
        return productIds.stream().distinct().map(id -> {
            int qty = available.getOrDefault(id, 0);
            return new StockAvailabilityResponse(id, qty, qty > 0);
        }).toList();
    }

    //    update a stock item
    @Transactional
    public StockItemResponse adjust(UUID productId, int delta) {
        StockItem item = stockLevelProducer.publishOnFlip(List.of(productId), () -> {
            if (delta > 0) {
                stockItemRepository.restock(productId, delta);
            } else if (stockItemRepository.adjust(productId, delta) == 0) {
                find(productId); // stock item may be absent
                throw new ResponseStatusException(HttpStatus.CONFLICT, "On hand cannot go below reserved");
            }
            return find(productId);
        });
        return StockItemResponse.from(item);
    }

    private StockItem find(UUID productId) {
        return stockItemRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
