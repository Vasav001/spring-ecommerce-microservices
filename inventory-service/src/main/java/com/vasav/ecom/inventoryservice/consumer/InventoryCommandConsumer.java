package com.vasav.ecom.inventoryservice.consumer;

import com.vasav.ecom.inventoryservice.events.*;
import com.vasav.ecom.inventoryservice.service.ReservationService;
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
public class InventoryCommandConsumer {

    private final ReservationService reservationService;
    private final JsonMapper jsonMapper;

    private static String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_COMMANDS, groupId = "inventory-service.commands")
    public void onCommand(ConsumerRecord<String, String> record) {
        String eventId = header(record, OutboxHeaders.EVENT_ID);
        String eventType = header(record, OutboxHeaders.EVENT_TYPE);

        if (eventId == null || eventType == null || record.value() == null) {
            log.warn("Skipping inventory command without event-id, event-type or value, key {}", record.key());
            return;
        }

        UUID id = UUID.fromString(eventId);
        switch (eventType) {
            case EventTypes.RESERVE_INVENTORY -> reservationService.reserve(
                    id, jsonMapper.readValue(record.value(), ReserveInventory.class)
            );
            case EventTypes.CONFIRM_RESERVATION -> reservationService.confirm(id, jsonMapper.readValue(
                    record.value(), ConfirmReservation.class).orderId()
            );
            case EventTypes.RELEASE_RESERVATION -> reservationService.release(
                    id, jsonMapper.readValue(record.value(), ReleaseReservation.class).orderId()
            );
            default -> log.warn("Skipping unknown inventory command {} for key {}", eventType, record.key());
        }
    }
}
