package com.vasav.ecom.integrationservice.producer;

import com.vasav.ecom.integrationservice.events.KafkaTopics;
import com.vasav.ecom.integrationservice.events.ShipmentEvent;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShipmentEventProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void publish(UUID orderId, String eventType, String reason) {
        outboxService.save(KafkaTopics.SHIPMENT_EVENTS, orderId.toString(), eventType,
                jsonMapper.writeValueAsBytes(new ShipmentEvent(orderId, reason)));
    }
}
