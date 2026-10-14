package com.vasav.ecom.searchservice.dto;

import com.vasav.ecom.searchservice.entity.SearchProduct;

import java.math.BigDecimal;
import java.util.UUID;

public record SearchResult(
        UUID productId,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean inStock,
        BigDecimal averageRating,
        long reviewCount
) {
    public static SearchResult from(SearchProduct product) {
        return new SearchResult(product.getProductId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getImageUrl(), product.isInStock(), product.getAverageRating(),
                product.getReviewCount());
    }
}
