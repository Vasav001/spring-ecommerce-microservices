package com.vasav.ecom.orderservice.producer;

import com.vasav.ecom.orderservice.dto.ShippingAddress;
import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.events.EventTypes;
import com.vasav.ecom.orderservice.events.KafkaTopics;
import com.vasav.ecom.orderservice.events.OrderConfirmed;
import com.vasav.ecom.orderservice.events.OrderFailed;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void confirmed(Order order) {
        OrderConfirmed event = new OrderConfirmed(order.getId(), order.getCustomerId(), order.getTotalAmount(),
                order.getItems().stream()
                        .map(i -> new OrderConfirmed.Item(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity()))
                        .toList(),
                ShippingAddress.from(order));
        send(order, EventTypes.ORDER_CONFIRMED, event);
    }

    public void failed(Order order) {
        send(order, EventTypes.ORDER_FAILED, new OrderFailed(order.getId(), order.getCustomerId(), order.getFailureReason()));
    }

    private void send(Order order, String eventType, Object event) {
        outboxService.save(KafkaTopics.ORDER_EVENTS, order.getId().toString(), eventType,
                jsonMapper.writeValueAsBytes(event));
    }
}
