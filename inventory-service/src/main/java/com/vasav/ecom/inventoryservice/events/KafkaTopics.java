package com.vasav.ecom.inventoryservice.events;

public final class KafkaTopics {

    public static final String INVENTORY_COMMANDS = "inventory.commands";
    public static final String INVENTORY_REPLIES = "inventory.replies";
    public static final String INVENTORY_STOCK_LEVELS = "inventory.stock-levels";
    public static final String CATALOG_PRODUCTS = "catalog.products";

    private KafkaTopics() {
    }
}
