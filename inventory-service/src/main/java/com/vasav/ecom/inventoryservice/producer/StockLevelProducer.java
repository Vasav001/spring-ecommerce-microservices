package com.vasav.ecom.inventoryservice.producer;

import com.vasav.ecom.inventoryservice.events.KafkaTopics;
import com.vasav.ecom.inventoryservice.events.StockLevelChanged;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockLevelProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    //  send StockLevelChanged only when a product goes in / out of stock
    public void publishIfChanged(UUID productId, boolean wasInStock, boolean inStock) {
        if (wasInStock == inStock) {
            return;
        }
        StockLevelChanged event = new StockLevelChanged(productId, inStock);
//        send msg via kafka
//        outbox holds the serialized msg. we serialize using jackson3
        outboxService.save(KafkaTopics.INVENTORY_STOCK_LEVELS, productId.toString(),
                "StockLevelChanged", jsonMapper.writeValueAsBytes(event));
    }
}
