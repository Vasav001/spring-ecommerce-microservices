package com.vasav.ecom.catalogservice.service;

import com.vasav.ecom.catalogservice.events.EventTypes;
import com.vasav.ecom.catalogservice.events.OrderEvent;
import com.vasav.ecom.catalogservice.repository.ProcessedEventRepository;
import com.vasav.ecom.catalogservice.repository.PurchasedItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private static final String CONSUMER = "catalog-service.reviews";

    private final PurchasedItemRepository purchasedItemRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void onOrderEvent(UUID eventId, String eventType, OrderEvent event) {
        if (processedEventRepository.markProcessed(eventId, CONSUMER) == 0) {
            return;
        }
        switch (eventType) {
            case EventTypes.ORDER_CONFIRMED -> event.items().forEach(item ->
                    purchasedItemRepository.record(event.orderId(), item.productId(), event.customerId()));
            case EventTypes.ORDER_DELIVERED -> purchasedItemRepository.markDelivered(event.orderId());
            case EventTypes.ORDER_FAILED -> purchasedItemRepository.deleteByOrderId(event.orderId());
            default -> {
            }
        }
    }
}
