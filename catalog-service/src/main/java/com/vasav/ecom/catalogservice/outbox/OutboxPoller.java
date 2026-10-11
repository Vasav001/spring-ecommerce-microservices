package com.vasav.ecom.catalogservice.outbox;

import com.vasav.ecom.catalogservice.entity.OutboxEvent;
import com.vasav.ecom.catalogservice.events.EventHeaders;
import com.vasav.ecom.catalogservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.outbox.batch-size:10}")
    private int batchSize;

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:200}")
    @Transactional
    public void poll() {
        // one poller at a time, needed when multi instance of service
        if (!repository.tryLock()) {
            return;
        }

        for (OutboxEvent event : repository.findPending(batchSize)) {
            try {
                kafkaTemplate.send(toRecord(event)).get(10, TimeUnit.SECONDS);
                event.setPublishedAt(Instant.now());
            } catch (Exception e) {
                log.warn("Outbox publish failed for event {}, will retry", event.getId(), e);
                break;
            }
        }
    }

    private ProducerRecord<String, String> toRecord(OutboxEvent e) {
        var record = new ProducerRecord<>(e.getTopic(), e.getAggregateId(), e.getPayload());
        record.headers()
                .add(EventHeaders.EVENT_ID, bytes(e.getId().toString()))
                .add(EventHeaders.EVENT_TYPE, bytes(e.getEventType()))
                .add(EventHeaders.EVENT_VERSION, bytes(String.valueOf(e.getEventVersion())))
                .add(EventHeaders.OCCURRED_AT, bytes(e.getCreatedAt().toString()));
        if (e.getCorrelationId() != null) {
            record.headers().add(EventHeaders.CORRELATION_ID, bytes(e.getCorrelationId()));
        }
        return record;
    }
}