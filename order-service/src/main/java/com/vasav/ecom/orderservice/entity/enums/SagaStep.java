package com.vasav.ecom.orderservice.entity.enums;

public enum SagaStep {
    RESERVING_INVENTORY,
    PROCESSING_PAYMENT,
    CONFIRMING_INVENTORY,
    RELEASING_INVENTORY,
    REFUNDING
}
