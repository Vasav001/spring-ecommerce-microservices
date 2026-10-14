package com.vasav.ecom.orderservice.service;

import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.entity.enums.OrderStatus;
import com.vasav.ecom.orderservice.events.EventTypes;
import com.vasav.ecom.orderservice.producer.InventoryCommandProducer;
import com.vasav.ecom.orderservice.producer.OrderEventProducer;
import com.vasav.ecom.orderservice.producer.PaymentCommandProducer;
import com.vasav.ecom.orderservice.repository.OrderRepository;
import com.vasav.ecom.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private static final String CONSUMER = "order-service.saga";

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OrderEventProducer orderEvents;
    private final PaymentCommandProducer paymentCommands;
    private final InventoryCommandProducer inventoryCommands;

    @Transactional
    public void onShipmentEvent(UUID eventId, String eventType, UUID orderId) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        switch (eventType) {
            case EventTypes.SHIPMENT_DISPATCHED -> {
                if (orderRepository.updateStatus(orderId, OrderStatus.CONFIRMED, OrderStatus.SHIPPED) > 0) {
                    orderEvents.shipped(order(orderId));
                }
            }
            case EventTypes.SHIPMENT_DELIVERED -> {
                if (orderRepository.updateStatus(orderId, OrderStatus.SHIPPED, OrderStatus.DELIVERED) > 0) {
                    orderEvents.delivered(order(orderId));
                }
            }
            case EventTypes.SHIPMENT_FAILED -> {
                if (orderRepository.updateStatus(orderId, List.of(OrderStatus.CONFIRMED, OrderStatus.SHIPPED),
                        OrderStatus.FAILED, "SHIPMENT_FAILED") > 0) {
                    Order order = order(orderId);
                    paymentCommands.refund(order);
                    inventoryCommands.returnToStock(order);
                    orderEvents.failed(order);
                }
            }
            default -> log.warn("Ignoring shipment event {} for order {}", eventType, orderId);
        }
    }

    private Order order(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow();
    }
}
