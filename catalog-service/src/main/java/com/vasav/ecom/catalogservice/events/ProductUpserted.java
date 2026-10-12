package com.vasav.ecom.catalogservice.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vasav.ecom.catalogservice.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductUpserted(
        UUID productId,
        long version,
        String sku,
        String name,
        String description,
        String brand,
        String category,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal price,
        String status,
        List<String> imageRefs
) {
    public static ProductUpserted from(Product p) {
        return new ProductUpserted(p.getId(), p.getVersion(),
                p.getSku(), p.getName(), p.getDescription(),
                p.getBrand(), p.getCategory(), p.getPrice(),
                p.getStatus().name(), List.copyOf(p.getImageRefs()));
    }
}
