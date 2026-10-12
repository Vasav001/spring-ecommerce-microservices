package com.vasav.ecom.catalogservice.dto.product;

import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank @Size(max = 100) String sku,
        @NotBlank @Size(max = 200) String name,
        String description,
        @Size(max = 100) String brand,
        @NotBlank @Size(max = 100) String category,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
        ProductStatus status,
        List<String> imageRefs
) {
    public ProductRequest {
        imageRefs = imageRefs == null ? List.of() : List.copyOf(imageRefs);
    }
}