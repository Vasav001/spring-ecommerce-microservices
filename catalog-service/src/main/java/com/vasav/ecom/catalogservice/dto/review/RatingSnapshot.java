package com.vasav.ecom.catalogservice.dto.review;

import java.util.UUID;

public interface RatingSnapshot {
    UUID getProductId();
    int getRatingCount();
    int getRatingSum();
    long getVersion();
}