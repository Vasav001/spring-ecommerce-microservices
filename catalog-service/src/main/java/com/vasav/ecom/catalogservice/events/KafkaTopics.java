package com.vasav.ecom.catalogservice.events;

public final class KafkaTopics {

    public static final String CATALOG_PRODUCTS = "catalog.products";
    public static final String REVIEW_PRODUCT_RATINGS = "review.product-ratings";
    public static final String ORDER_EVENTS = "order.events";

    private KafkaTopics() {
    }
}
