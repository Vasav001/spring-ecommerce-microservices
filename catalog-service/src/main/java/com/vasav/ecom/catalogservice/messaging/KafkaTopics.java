package com.vasav.ecom.catalogservice.messaging;

public final class KafkaTopics {
//    producer: "catalog.products", "catalog.product-ratings"
//    consumer: "order.events" and its DLT "order.events.DLT"
    public static final String CATALOG_PRODUCTS = "catalog.products";
    public static final String CATALOG_PRODUCT_RATINGS = "catalog.product-ratings";
    public static final String ORDER_EVENTS = "order.events";
    public static final String ORDER_EVENTS_DLT = "order.events.DLT";

    private KafkaTopics() {
    }
}
