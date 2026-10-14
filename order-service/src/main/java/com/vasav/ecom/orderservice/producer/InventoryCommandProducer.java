package com.vasav.ecom.orderservice.producer;

import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.events.EventTypes;
import com.vasav.ecom.orderservice.events.KafkaTopics;
import com.vasav.ecom.orderservice.events.ReserveInventory;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class InventoryCommandProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void reserve(Order order) {
        ReserveInventory command = new ReserveInventory(order.getId(), order.getItems().stream()
                .map(i -> new ReserveInventory.Item(i.getProductId(), i.getQuantity()))
                .toList());
        outboxService.save(KafkaTopics.INVENTORY_COMMANDS, order.getId().toString(),
                EventTypes.RESERVE_INVENTORY, jsonMapper.writeValueAsBytes(command));
    }
}
