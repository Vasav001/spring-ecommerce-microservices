package com.vasav.ecom.integrationservice.events;

public final class KafkaTopics {

    public static final String PAYMENT_COMMANDS = "payment.commands";
    public static final String PAYMENT_REPLIES = "payment.replies";
    public static final String ORDER_EVENTS = "order.events";
    public static final String SHIPMENT_EVENTS = "shipment.events";

    private KafkaTopics() {
    }
}
