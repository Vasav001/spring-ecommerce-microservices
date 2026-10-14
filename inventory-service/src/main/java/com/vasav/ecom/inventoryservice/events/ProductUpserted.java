package com.vasav.ecom.inventoryservice.events;

import java.util.UUID;

public record ProductUpserted(
        UUID productId
) {
}
