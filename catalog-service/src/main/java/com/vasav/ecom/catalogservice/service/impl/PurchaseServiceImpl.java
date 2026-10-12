package com.vasav.ecom.catalogservice.service.impl;

import com.vasav.ecom.catalogservice.repository.PurchaseRepository;
import com.vasav.ecom.catalogservice.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchases;

    @Override
    @Transactional
    public void recordDelivered(UUID orderId, String userId, List<UUID> productIds) {
        for (UUID productId : productIds) {
            purchases.insertIfAbsent(userId, productId, orderId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPurchased(String userId, UUID productId) {
        return purchases.existsByUserIdAndProductId(userId, productId);
    }
}