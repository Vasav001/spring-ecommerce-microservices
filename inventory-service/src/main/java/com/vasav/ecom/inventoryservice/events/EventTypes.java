package com.vasav.ecom.inventoryservice.events;

public final class EventTypes {

    public static final String RESERVE_INVENTORY = "ReserveInventory";
    public static final String CONFIRM_RESERVATION = "ConfirmReservation";
    public static final String RELEASE_RESERVATION = "ReleaseReservation";

    public static final String INVENTORY_RESERVED = "InventoryReserved";
    public static final String INVENTORY_RESERVATION_FAILED = "InventoryReservationFailed";
    public static final String RESERVATION_CONFIRMED = "ReservationConfirmed";
    public static final String RESERVATION_CONFIRM_FAILED = "ReservationConfirmFailed";
    public static final String RESERVATION_RELEASED = "ReservationReleased";
    public static final String RESERVATION_RELEASE_FAILED = "ReservationReleaseFailed";
    public static final String RESERVATION_EXPIRED = "ReservationExpired";

    public static final String STOCK_LEVEL_CHANGED = "StockLevelChanged";

    private EventTypes() {
    }
}
