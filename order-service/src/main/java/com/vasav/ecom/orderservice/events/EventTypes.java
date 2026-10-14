package com.vasav.ecom.orderservice.events;

public final class EventTypes {

    public static final String RESERVE_INVENTORY = "ReserveInventory";
    public static final String CONFIRM_RESERVATION = "ConfirmReservation";
    public static final String RELEASE_RESERVATION = "ReleaseReservation";
    public static final String RETURN_RESERVATION = "ReturnReservation";

    public static final String INVENTORY_RESERVED = "InventoryReserved";
    public static final String INVENTORY_RESERVATION_FAILED = "InventoryReservationFailed";
    public static final String RESERVATION_CONFIRMED = "ReservationConfirmed";
    public static final String RESERVATION_CONFIRM_FAILED = "ReservationConfirmFailed";
    public static final String RESERVATION_RELEASED = "ReservationReleased";
    public static final String RESERVATION_RELEASE_FAILED = "ReservationReleaseFailed";
    public static final String RESERVATION_EXPIRED = "ReservationExpired";

    public static final String PROCESS_PAYMENT = "ProcessPayment";
    public static final String REFUND_PAYMENT = "RefundPayment";

    public static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String PAYMENT_REFUNDED = "PaymentRefunded";

    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_FAILED = "OrderFailed";
    public static final String ORDER_SHIPPED = "OrderShipped";
    public static final String ORDER_DELIVERED = "OrderDelivered";

    public static final String SHIPMENT_DISPATCHED = "ShipmentDispatched";
    public static final String SHIPMENT_DELIVERED = "ShipmentDelivered";
    public static final String SHIPMENT_FAILED = "ShipmentFailed";

    private EventTypes() {
    }
}
