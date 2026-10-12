package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.Product;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsById(@NonNull UUID id);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}