package com.vasav.ecom.orderservice.events;

public final class KafkaTopics {

    public static final String INVENTORY_COMMANDS = "inventory.commands";
    public static final String INVENTORY_REPLIES = "inventory.replies";
    public static final String PAYMENT_COMMANDS = "payment.commands";
    public static final String PAYMENT_REPLIES = "payment.replies";
    public static final String ORDER_EVENTS = "order.events";
    public static final String SHIPMENT_EVENTS = "shipment.events";

    private KafkaTopics() {
    }
}
