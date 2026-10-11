package com.vasav.ecom.catalogservice.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final JdbcClient jdbcClient;

    @Transactional
    public void recordDelivered(UUID eventId, OrderDelivered event) {
        int inserted = jdbcClient.sql("""
                        INSERT INTO processed_event (consumer, event_id)
                        VALUES ('catalog-service.orders', ?)
                        ON CONFLICT DO NOTHING
                        """)
                .param(eventId)
                .update();
        if (inserted == 0) {
            return;
        }
        for (UUID productId : event.productIds()) {
            jdbcClient.sql("""
                            INSERT INTO purchase (user_id, product_id, order_id, created_at)
                            VALUES (?, ?, ?, now())
                            ON CONFLICT DO NOTHING
                            """)
                    .params(event.userId(), productId, event.orderId())
                    .update();
        }
    }
}
