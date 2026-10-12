package com.vasav.ecom.catalogservice.dto.product;

import com.vasav.ecom.catalogservice.dto.images.ImageView;
import com.vasav.ecom.catalogservice.entity.Product;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import com.vasav.ecom.catalogservice.util.image.ImageUrlResolver;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AdminProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String brand,
        String category,
        BigDecimal price,
        ProductStatus status,
        List<ImageView> images,
        Long version) {

    public static AdminProductResponse from(Product p, ImageUrlResolver resolver) {
        List<ImageView> images = p.getImageRefs().stream()
                .map(ref -> new ImageView(ref, resolver.toPublicUrl(ref)))
                .toList();

        return new AdminProductResponse(p.getId(), p.getSku(),
                p.getName(), p.getDescription(), p.getBrand(),
                p.getCategory(), p.getPrice(), p.getStatus(),
                images, p.getVersion());
    }
}