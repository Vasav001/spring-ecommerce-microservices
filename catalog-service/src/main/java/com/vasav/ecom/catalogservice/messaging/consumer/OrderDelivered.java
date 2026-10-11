package com.vasav.ecom.catalogservice.messaging.consumer;

import java.util.List;
import java.util.UUID;

public record OrderDelivered(UUID orderId, String userId, List<UUID> productIds) {
}
