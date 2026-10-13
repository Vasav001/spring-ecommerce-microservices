package com.vasav.ecom.inventoryservice.producer;

import com.vasav.ecom.inventoryservice.events.EventTypes;
import com.vasav.ecom.inventoryservice.events.KafkaTopics;
import com.vasav.ecom.inventoryservice.events.StockLevelChanged;
import com.vasav.ecom.inventoryservice.repository.StockItemRepository;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class StockLevelProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;
    private final StockItemRepository stockItemRepository;

    //  runs the stock change, then sends StockLevelChanged only for products that went in / out of stock
    public <T> T publishOnFlip(Collection<UUID> productIds, Supplier<T> change) {
        Set<UUID> inStockBefore = inStock(productIds);
        T result = change.get();
        Set<UUID> inStockAfter = inStock(productIds);
        for (UUID productId : productIds) {
            boolean inStock = inStockAfter.contains(productId);
            // check flip
            if (inStockBefore.contains(productId) != inStock) {
                StockLevelChanged event = new StockLevelChanged(productId, inStock);
//        send msg via kafka
//        outbox holds the serialized msg. we serialize using jackson3
                outboxService.save(KafkaTopics.INVENTORY_STOCK_LEVELS, productId.toString(),
                        EventTypes.STOCK_LEVEL_CHANGED, jsonMapper.writeValueAsBytes(event));
            }
        }
        return result;
    }

    private Set<UUID> inStock(Collection<UUID> productIds) {
        Set<UUID> result = new HashSet<>();
        stockItemRepository.findAllById(productIds).forEach(i -> {
            if (i.available() > 0) {
                result.add(i.getProductId());
            }
        });
        return result;
    }
}
