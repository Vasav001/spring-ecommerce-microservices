package com.vasav.ecom.integrationservice.consumer;

import com.vasav.ecom.integrationservice.events.EventTypes;
import com.vasav.ecom.integrationservice.events.KafkaTopics;
import com.vasav.ecom.integrationservice.events.OrderConfirmed;
import com.vasav.ecom.integrationservice.service.ShipmentService;
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
public class OrderEventConsumer {

    private final ShipmentService shipmentService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "integration-service.shipping")
    public void onOrderEvent(ConsumerRecord<String, String> record) {
        String eventId = header(record, OutboxHeaders.EVENT_ID);
        String eventType = header(record, OutboxHeaders.EVENT_TYPE);
        if (eventId == null || eventType == null || record.value() == null) {
            log.warn("Skipping order event without event-id, event-type or value, key {}", record.key());
            return;
        }
        if (EventTypes.ORDER_CONFIRMED.equals(eventType)) {
            shipmentService.create(UUID.fromString(eventId), jsonMapper.readValue(record.value(), OrderConfirmed.class));
        }
    }

    private static String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
