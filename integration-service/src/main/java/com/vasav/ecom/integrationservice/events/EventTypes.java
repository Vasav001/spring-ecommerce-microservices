package com.vasav.ecom.integrationservice.events;

public final class EventTypes {

    public static final String PROCESS_PAYMENT = "ProcessPayment";
    public static final String REFUND_PAYMENT = "RefundPayment";

    public static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String PAYMENT_REFUNDED = "PaymentRefunded";

    public static final String ORDER_CONFIRMED = "OrderConfirmed";

    public static final String SHIPMENT_DISPATCHED = "ShipmentDispatched";
    public static final String SHIPMENT_DELIVERED = "ShipmentDelivered";
    public static final String SHIPMENT_FAILED = "ShipmentFailed";

    private EventTypes() {
    }
}
