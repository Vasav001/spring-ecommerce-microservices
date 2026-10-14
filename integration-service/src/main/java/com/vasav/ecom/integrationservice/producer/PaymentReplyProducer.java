package com.vasav.ecom.integrationservice.producer;

import com.vasav.ecom.integrationservice.events.KafkaTopics;
import com.vasav.ecom.integrationservice.events.PaymentReply;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentReplyProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void reply(UUID orderId, String eventType, String reason) {
        outboxService.save(KafkaTopics.PAYMENT_REPLIES, orderId.toString(), eventType,
                jsonMapper.writeValueAsBytes(new PaymentReply(orderId, reason)));
    }
}
