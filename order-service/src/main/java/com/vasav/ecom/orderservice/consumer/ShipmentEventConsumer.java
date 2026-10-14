package com.vasav.ecom.orderservice.consumer;

import com.vasav.ecom.orderservice.events.KafkaTopics;
import com.vasav.ecom.orderservice.events.ShipmentEvent;
import com.vasav.ecom.orderservice.service.FulfillmentService;
import com.vasav.ecom.outbox.OutboxHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventConsumer {

    private final FulfillmentService fulfillmentService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = KafkaTopics.SHIPMENT_EVENTS, groupId = "order-service.saga")
    public void onShipmentEvent(ConsumerRecord<String, String> record) {
        String eventId = header(record, OutboxHeaders.EVENT_ID);
        String eventType = header(record, OutboxHeaders.EVENT_TYPE);
        if (eventId == null || eventType == null || record.value() == null) {
            log.warn("Skipping shipment record without event-id, event-type or value, key {}", record.key());
            return;
        }
        ShipmentEvent event = jsonMapper.readValue(record.value(), ShipmentEvent.class);
        fulfillmentService.onShipmentEvent(UUID.fromString(eventId), eventType, event.orderId());
    }

    private static String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
