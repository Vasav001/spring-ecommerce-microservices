package com.vasav.ecom.catalogservice.messaging;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        String aggregateId,
        String correlationId,
        T payload
) {
}
