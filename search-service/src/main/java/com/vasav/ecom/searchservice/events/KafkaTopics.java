package com.vasav.ecom.searchservice.events;

public final class KafkaTopics {

    public static final String CATALOG_PRODUCTS = "catalog.products";
    public static final String INVENTORY_STOCK_LEVELS = "inventory.stock-levels";
    public static final String REVIEW_PRODUCT_RATINGS = "review.product-ratings";

    private KafkaTopics() {
    }
}
