package com.vasav.ecom.searchservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SearchFilter(
        @Size(max = 100) String q,
        @DecimalMin("0") BigDecimal minPrice,
        @DecimalMin("0") BigDecimal maxPrice,
        Boolean inStock,
        @DecimalMin("0") @DecimalMax("5") BigDecimal minRating
) {
    @AssertTrue(message = "minPrice must not be greater than maxPrice")
    public boolean isPriceRangeValid() {
        return minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0;
    }
}
