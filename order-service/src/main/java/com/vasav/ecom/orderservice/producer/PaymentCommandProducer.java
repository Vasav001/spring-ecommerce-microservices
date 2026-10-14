package com.vasav.ecom.orderservice.producer;

import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.events.EventTypes;
import com.vasav.ecom.orderservice.events.KafkaTopics;
import com.vasav.ecom.orderservice.events.ProcessPayment;
import com.vasav.ecom.orderservice.events.RefundPayment;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class PaymentCommandProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void process(Order order) {
        send(order, EventTypes.PROCESS_PAYMENT,
                new ProcessPayment(order.getId(), order.getCustomerId(), order.getTotalAmount()));
    }

    public void refund(Order order) {
        send(order, EventTypes.REFUND_PAYMENT, new RefundPayment(order.getId(), order.getTotalAmount()));
    }

    private void send(Order order, String eventType, Object command) {
        outboxService.save(KafkaTopics.PAYMENT_COMMANDS, order.getId().toString(), eventType,
                jsonMapper.writeValueAsBytes(command));
    }
}
