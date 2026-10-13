package com.vasav.ecom.inventoryservice.service;

import com.vasav.ecom.inventoryservice.entity.StockItem;
import com.vasav.ecom.inventoryservice.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogProjectionService {
    //  inventory keeps track of whether a product is active or not
    //  stock item has active field that changes on catalog event
    //  stock item has product id and active
    private final StockItemRepository stockItemRepository;

    @Transactional
    public void apply(UUID productId, boolean active) {
        StockItem item = stockItemRepository.findById(productId).orElseGet(() -> new StockItem(productId));
        item.setActive(active);
        stockItemRepository.save(item);
    }
}
