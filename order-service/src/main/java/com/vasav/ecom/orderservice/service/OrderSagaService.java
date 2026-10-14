package com.vasav.ecom.orderservice.service;

import com.vasav.ecom.orderservice.entity.Order;
import com.vasav.ecom.orderservice.entity.OrderSaga;
import com.vasav.ecom.orderservice.entity.enums.OrderStatus;
import com.vasav.ecom.orderservice.entity.enums.SagaStep;
import com.vasav.ecom.orderservice.events.EventTypes;
import com.vasav.ecom.orderservice.producer.InventoryCommandProducer;
import com.vasav.ecom.orderservice.producer.OrderEventProducer;
import com.vasav.ecom.orderservice.producer.PaymentCommandProducer;
import com.vasav.ecom.orderservice.repository.OrderRepository;
import com.vasav.ecom.orderservice.repository.OrderSagaRepository;
import com.vasav.ecom.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

import static com.vasav.ecom.orderservice.entity.enums.SagaStep.CONFIRMING_INVENTORY;
import static com.vasav.ecom.orderservice.entity.enums.SagaStep.PROCESSING_PAYMENT;
import static com.vasav.ecom.orderservice.entity.enums.SagaStep.REFUNDING;
import static com.vasav.ecom.orderservice.entity.enums.SagaStep.RELEASING_INVENTORY;
import static com.vasav.ecom.orderservice.entity.enums.SagaStep.RESERVING_INVENTORY;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private static final String CONSUMER = "order-service.saga";

    private final OrderRepository orderRepository;
    private final OrderSagaRepository orderSagaRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryCommandProducer inventoryCommands;
    private final PaymentCommandProducer paymentCommands;
    private final OrderEventProducer orderEvents;

    @Value("${order.saga.step-timeout:2m}")
    private Duration stepTimeout;

    @Transactional
    public void onReply(UUID eventId, String eventType, UUID orderId, String reason) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        switch (eventType) {
            case EventTypes.INVENTORY_RESERVED -> next(orderId, RESERVING_INVENTORY, PROCESSING_PAYMENT, paymentCommands::process);
            case EventTypes.INVENTORY_RESERVATION_FAILED -> fail(orderId, RESERVING_INVENTORY, reason, o -> { });
            case EventTypes.PAYMENT_SUCCEEDED -> next(orderId, PROCESSING_PAYMENT, CONFIRMING_INVENTORY, inventoryCommands::confirm);
            case EventTypes.PAYMENT_FAILED -> compensate(orderId, PROCESSING_PAYMENT, RELEASING_INVENTORY, "PAYMENT_FAILED", inventoryCommands::release);
            case EventTypes.RESERVATION_CONFIRMED -> confirm(orderId);
            case EventTypes.RESERVATION_CONFIRM_FAILED -> compensate(orderId, CONFIRMING_INVENTORY, REFUNDING, reason, paymentCommands::refund);
            case EventTypes.RESERVATION_RELEASED -> fail(orderId, RELEASING_INVENTORY, null, o -> { });
            case EventTypes.RESERVATION_EXPIRED -> {
                if (!fail(orderId, RELEASING_INVENTORY, null, o -> { })) {
                    fail(orderId, PROCESSING_PAYMENT, "RESERVATION_EXPIRED", paymentCommands::refund);
                }
            }
            case EventTypes.PAYMENT_REFUNDED -> fail(orderId, REFUNDING, null, o -> { });
            default -> log.warn("Ignoring saga reply {} for order {}", eventType, orderId);
        }
    }

    @Transactional
    public void timeout(UUID orderId) {
        OrderSaga saga = orderSagaRepository.findById(orderId).orElse(null);
        if (saga == null || saga.getStep() == null || saga.getStepDeadline().isAfter(Instant.now())) {
            return;
        }
        log.warn("Saga step {} timed out for order {}", saga.getStep(), orderId);
        switch (saga.getStep()) {
            case RESERVING_INVENTORY -> fail(orderId, RESERVING_INVENTORY, "TIMEOUT", inventoryCommands::release);
            case PROCESSING_PAYMENT -> fail(orderId, PROCESSING_PAYMENT, "TIMEOUT", o -> {
                paymentCommands.refund(o);
                inventoryCommands.release(o);
            });
            case CONFIRMING_INVENTORY -> next(orderId, CONFIRMING_INVENTORY, CONFIRMING_INVENTORY, inventoryCommands::confirm);
            case RELEASING_INVENTORY -> fail(orderId, RELEASING_INVENTORY, null, o -> { });
            case REFUNDING -> next(orderId, REFUNDING, REFUNDING, paymentCommands::refund);
        }
    }

    private boolean next(UUID orderId, SagaStep from, SagaStep to, Consumer<Order> action) {
        if (orderSagaRepository.advance(orderId, from, to, Instant.now().plus(stepTimeout)) == 0) {
            return false;
        }
        action.accept(order(orderId));
        return true;
    }

    private void compensate(UUID orderId, SagaStep from, SagaStep to, String reason, Consumer<Order> action) {
        if (next(orderId, from, to, action)) {
            order(orderId).setFailureReason(reason);
        }
    }

    private boolean fail(UUID orderId, SagaStep from, String reason, Consumer<Order> action) {
        if (orderSagaRepository.advance(orderId, from, null, null) == 0) {
            return false;
        }
        Order order = order(orderId);
        order.setStatus(OrderStatus.FAILED);
        if (reason != null || order.getFailureReason() == null) {
            order.setFailureReason(reason == null ? "FAILED" : reason);
        }
        action.accept(order);
        orderEvents.failed(order);
        return true;
    }

    private void confirm(UUID orderId) {
        if (orderSagaRepository.advance(orderId, CONFIRMING_INVENTORY, null, null) == 0) {
            return;
        }
        Order order = order(orderId);
        order.setStatus(OrderStatus.CONFIRMED);
        orderEvents.confirmed(order);
    }

    private Order order(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow();
    }
}
