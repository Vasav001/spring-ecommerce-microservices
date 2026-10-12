package com.vasav.ecom.catalogservice.service;

import com.vasav.ecom.catalogservice.dto.product.AdminProductResponse;
import com.vasav.ecom.catalogservice.dto.product.ProductRequest;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    AdminProductResponse create(UUID productId, ProductRequest request);

    AdminProductResponse update(UUID productId, long expectedVersion, ProductRequest request);

    AdminProductResponse get(UUID productId);

    Page<AdminProductResponse> getAll(ProductStatus status, Pageable pageable);
}