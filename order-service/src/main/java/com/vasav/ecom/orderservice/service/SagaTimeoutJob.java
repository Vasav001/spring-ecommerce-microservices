package com.vasav.ecom.orderservice.service;

import com.vasav.ecom.orderservice.repository.OrderSagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaTimeoutJob {

    private final OrderSagaRepository orderSagaRepository;
    private final OrderSagaService orderSagaService;

    @Scheduled(fixedDelayString = "${order.saga.timeout-check-interval:30s}")
    public void checkTimeouts() {
        for (UUID orderId : orderSagaRepository.findOrderIdsByDeadlineBefore(Instant.now(), Limit.of(100))) {
            try {
                orderSagaService.timeout(orderId);
            } catch (Exception e) {
                log.error("Failed to handle saga timeout for order {}", orderId, e);
            }
        }
    }
}
