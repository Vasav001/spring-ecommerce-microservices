package com.vasav.ecom.catalogservice.consumer;

import com.vasav.ecom.catalogservice.config.KafkaTopics;
import com.vasav.ecom.catalogservice.events.EventHeaders;
import com.vasav.ecom.catalogservice.events.OrderDelivered;
import com.vasav.ecom.catalogservice.exception.UnknownEventVersionException;
import com.vasav.ecom.catalogservice.repository.ProcessedEventRepository;
import com.vasav.ecom.catalogservice.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderListener {

    private static final String ORDER_DELIVERED = "OrderDelivered";

    private final ProcessedEventRepository processedEvents;
    private final PurchaseRepository purchases;
    private final ObjectMapper objectMapper;

    private static String header(ConsumerRecord<?, ?> record, String name) {
        Header h = record.headers().lastHeader(name);
        if (h == null) {
            throw new IllegalArgumentException("Missing header " + name);   // make it non-retryable → DLT
        }
        return new String(h.value(), StandardCharsets.UTF_8);
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = KafkaTopics.GROUP_ORDERS)
    @Transactional
    public void onMessage(ConsumerRecord<String, String> record) throws Exception {
        String eventType = header(record, EventHeaders.EVENT_TYPE);
        if (!ORDER_DELIVERED.equals(eventType)) {
            return;
        }

        int version = Integer.parseInt(header(record, EventHeaders.EVENT_VERSION));
        if (version != 1) {
            throw new UnknownEventVersionException(eventType, version);
        }

        UUID eventId = UUID.fromString(header(record, EventHeaders.EVENT_ID));
        if (processedEvents.markProcessed(KafkaTopics.GROUP_ORDERS, eventId) == 0) {
            log.debug("Duplicate event {} skipped", eventId);
            return;
        }

        OrderDelivered event = objectMapper.readValue(record.value(), OrderDelivered.class);
        for (UUID productId : event.productIds()) {
            purchases.insertIfAbsent(event.userId(), productId, event.orderId());
        }
    }
}