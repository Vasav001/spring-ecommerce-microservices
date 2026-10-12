package com.vasav.ecom.catalogservice.consumer;

import com.vasav.ecom.catalogservice.events.KafkaTopics;
import com.vasav.ecom.catalogservice.events.EventHeaders;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsDltListener {

    private final MeterRegistry meterRegistry;

    private static String text(ConsumerRecord<?, ?> r, String name) {
        Header h = r.headers().lastHeader(name);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }

    private static Integer intHeader(ConsumerRecord<?, ?> r, String name) {
        Header h = r.headers().lastHeader(name);
        return h == null ? null : ByteBuffer.wrap(h.value()).getInt();
    }

    private static Long longHeader(ConsumerRecord<?, ?> r, String name) {
        Header h = r.headers().lastHeader(name);
        return h == null ? null : ByteBuffer.wrap(h.value()).getLong();
    }

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS_DLT, groupId = KafkaTopics.GROUP_ORDERS + ".dlt")
    public void onDeadLetter(ConsumerRecord<String, String> record) {
        try {
            log.error("Dead letter: originalTopic={} partition={} offset={} key={} eventId={} eventType={} exception={} cause={} message={}",
                    text(record, KafkaHeaders.DLT_ORIGINAL_TOPIC),
                    intHeader(record, KafkaHeaders.DLT_ORIGINAL_PARTITION),
                    longHeader(record, KafkaHeaders.DLT_ORIGINAL_OFFSET),
                    record.key(),
                    text(record, EventHeaders.EVENT_ID),
                    text(record, EventHeaders.EVENT_TYPE),
                    text(record, KafkaHeaders.DLT_EXCEPTION_FQCN),
                    text(record, KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN),
                    text(record, KafkaHeaders.DLT_EXCEPTION_MESSAGE));

            meterRegistry.counter("kafka_dlt_messages_total", "topic", KafkaTopics.ORDER_EVENTS).increment();
        } catch (Exception e) {
            log.error("Failed to log dead letter at offset {}", record.offset(), e);   // never throw from here
        }
    }
}