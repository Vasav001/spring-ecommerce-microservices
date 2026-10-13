package com.vasav.ecom.inventoryservice.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogProductEvent(
//        Inventory only needs to keep track of Catalog product's status (active or not)
        String status
) {
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
