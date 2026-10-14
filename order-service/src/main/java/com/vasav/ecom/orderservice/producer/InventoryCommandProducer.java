package com.vasav.ecom.orderservice.producer;

import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.events.ConfirmReservation;
import com.vasav.ecom.orderservice.events.EventTypes;
import com.vasav.ecom.orderservice.events.KafkaTopics;
import com.vasav.ecom.orderservice.events.ReleaseReservation;
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
        send(order, EventTypes.RESERVE_INVENTORY, command);
    }

    public void confirm(Order order) {
        send(order, EventTypes.CONFIRM_RESERVATION, new ConfirmReservation(order.getId()));
    }

    public void release(Order order) {
        send(order, EventTypes.RELEASE_RESERVATION, new ReleaseReservation(order.getId()));
    }

    private void send(Order order, String eventType, Object command) {
        outboxService.save(KafkaTopics.INVENTORY_COMMANDS, order.getId().toString(), eventType,
                jsonMapper.writeValueAsBytes(command));
    }
}
