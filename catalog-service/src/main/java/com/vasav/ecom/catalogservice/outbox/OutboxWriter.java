package com.vasav.ecom.catalogservice.outbox;

import com.vasav.ecom.catalogservice.entity.OutboxEvent;
import com.vasav.ecom.catalogservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxEventRepository outbox;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    public void append(String topic, String aggregateId, String eventType, Object payload) {
        OutboxEvent event = new OutboxEvent();
        event.setTopic(topic);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setCorrelationId(MDC.get("correlationId"));
        event.setPayload(objectMapper.writeValueAsString(payload));
        outbox.save(event);
    }
}