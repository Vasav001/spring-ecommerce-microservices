package com.vasav.ecom.catalogservice.dto;

import com.vasav.ecom.catalogservice.entity.Product;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        ProductStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getImageUrl(), product.getStatus(), product.getCreatedAt(), product.getUpdatedAt());
    }
}
