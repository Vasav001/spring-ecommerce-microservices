package com.vasav.ecom.inventoryservice.producer;

import com.vasav.ecom.inventoryservice.events.InventoryReply;
import com.vasav.ecom.inventoryservice.events.KafkaTopics;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryReplyProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void reply(UUID orderId, String eventType) {
        reply(orderId, eventType, null);
    }

    public void reply(UUID orderId, String eventType, String reason) {
        outboxService.save(KafkaTopics.INVENTORY_REPLIES, orderId.toString(), eventType,
                jsonMapper.writeValueAsBytes(new InventoryReply(orderId, reason)));
    }
}
