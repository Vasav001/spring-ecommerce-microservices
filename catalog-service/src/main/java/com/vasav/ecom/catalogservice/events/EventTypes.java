package com.vasav.ecom.catalogservice.events;

public final class EventTypes {

    public static final String PRODUCT_UPSERTED = "ProductUpserted";
    public static final String PRODUCT_RATING_CHANGED = "ProductRatingChanged";

    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_DELIVERED = "OrderDelivered";
    public static final String ORDER_FAILED = "OrderFailed";

    private EventTypes() {
    }
}
