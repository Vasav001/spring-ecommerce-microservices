package com.vasav.ecom.inventoryservice.events;

import java.util.UUID;

public record InventoryReply(
        UUID orderId,
        String reason
) {
}
