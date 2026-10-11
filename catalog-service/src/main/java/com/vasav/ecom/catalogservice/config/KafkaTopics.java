package com.vasav.ecom.catalogservice.config;

public final class KafkaTopics {
    //    dummy: hello
    //    producer: "catalog.products", "catalog.product-ratings"
    //    consumer: "order.events" and its DLT "order.events.DLT"
    //    consumer group: "catalog-service.orders"
    public static final String CATALOG_PRODUCTS = "catalog.products";
    public static final String CATALOG_PRODUCT_RATINGS = "catalog.product-ratings";

    public static final String ORDER_EVENTS = "order.events";
    public static final String ORDER_EVENTS_DLT = "order.events.DLT";

    public static final String GROUP_ORDERS = "catalog-service.orders";

    private KafkaTopics() {
    }
}
