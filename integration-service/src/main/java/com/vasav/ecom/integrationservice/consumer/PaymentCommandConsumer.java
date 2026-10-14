package com.vasav.ecom.integrationservice.consumer;

import com.vasav.ecom.integrationservice.events.EventTypes;
import com.vasav.ecom.integrationservice.events.KafkaTopics;
import com.vasav.ecom.integrationservice.events.ProcessPayment;
import com.vasav.ecom.integrationservice.events.RefundPayment;
import com.vasav.ecom.integrationservice.service.PaymentService;
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
public class PaymentCommandConsumer {

    private final PaymentService paymentService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMMANDS, groupId = "integration-service.payments")
    public void onCommand(ConsumerRecord<String, String> record) {
        String eventId = header(record, OutboxHeaders.EVENT_ID);
        String eventType = header(record, OutboxHeaders.EVENT_TYPE);
        if (eventId == null || eventType == null || record.value() == null) {
            log.warn("Skipping payment command without event-id, event-type or value, key {}", record.key());
            return;
        }
        UUID id = UUID.fromString(eventId);
        switch (eventType) {
            case EventTypes.PROCESS_PAYMENT ->
                    paymentService.process(id, jsonMapper.readValue(record.value(), ProcessPayment.class));
            case EventTypes.REFUND_PAYMENT ->
                    paymentService.refund(id, jsonMapper.readValue(record.value(), RefundPayment.class).orderId());
            default -> log.warn("Skipping unknown payment command {} for key {}", eventType, record.key());
        }
    }

    private static String header(ConsumerRecord<String, String> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
