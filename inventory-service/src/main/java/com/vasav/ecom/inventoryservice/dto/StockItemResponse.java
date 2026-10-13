package com.vasav.ecom.inventoryservice.dto;

import com.vasav.ecom.inventoryservice.entity.StockItem;

import java.util.UUID;

public record StockItemResponse(
        UUID productId,
        int onHand,
        int reserved,
        int available
) {
    public static StockItemResponse from(StockItem item) {
        return new StockItemResponse(
                item.getProductId(),
                item.getOnHand(),
                item.getReserved(),
                item.available()
        );
    }
}
