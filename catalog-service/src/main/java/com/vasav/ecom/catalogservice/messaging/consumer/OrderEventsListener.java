package com.vasav.ecom.catalogservice.messaging.consumer;

import com.vasav.ecom.catalogservice.messaging.EventEnvelope;
import com.vasav.ecom.catalogservice.messaging.KafkaTopics;
import com.vasav.ecom.catalogservice.messaging.PoisonMessageException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OrderEventsListener {

    private final JsonMapper jsonMapper;
    private final PurchaseService purchaseService;

//      order events topic has OrderDelivered event only to be consumed by our consumer
    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "catalog-service.orders")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        Header type = record.headers().lastHeader("eventType");
        if (type == null || !"OrderDelivered".equals(new String(type.value(), StandardCharsets.UTF_8))) {
            return;
        }
        try {
            EventEnvelope<JsonNode> envelope = jsonMapper.readValue(record.value(), new TypeReference<>() {
            });
            OrderDelivered event = jsonMapper.treeToValue(envelope.payload(), OrderDelivered.class);
            purchaseService.recordDelivered(envelope.eventId(), event);
        } catch (JacksonException e) {
            throw new PoisonMessageException("Invalid OrderDelivered", e);
        }
    }
}
