package com.vasav.ecom.catalogservice.service;

import java.util.List;
import java.util.UUID;

public interface PurchaseService {
    void recordDelivered(UUID orderId, String userId, List<UUID> productIds);

    boolean hasPurchased(String userId, UUID productId);

}
