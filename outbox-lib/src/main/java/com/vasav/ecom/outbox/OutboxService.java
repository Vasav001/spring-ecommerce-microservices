package com.vasav.ecom.outbox;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class OutboxService {

    private final JdbcTemplate jdbc;
    private final Tracer tracer;
    private final Propagator propagator;
    private final JsonMapper json = JsonMapper.builder().build();

    public OutboxService(JdbcTemplate jdbc, Tracer tracer, Propagator propagator) {
        this.jdbc = jdbc;
        this.tracer = tracer;
        this.propagator = propagator;
    }

    public UUID save(String topic, String key, String eventType, byte[] value) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("OutboxService.save() must run inside a transaction");
        }
        if (key != null) {
            jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> null, topic + ":" + key);
        }

        UUID eventId = UUID.randomUUID();
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(OutboxHeaders.EVENT_ID, eventId.toString());
        headers.put(OutboxHeaders.EVENT_TYPE, eventType);
        Span span = tracer != null ? tracer.currentSpan() : null;
        if (span != null && propagator != null) {
            propagator.inject(span.context(), headers, Map::put);
        }

        jdbc.update("INSERT INTO outbox_record (topic, record_key, value, headers) VALUES (?, ?, ?, ?::jsonb)",
                topic, key, value, json.writeValueAsString(headers));
        return eventId;
    }
}
