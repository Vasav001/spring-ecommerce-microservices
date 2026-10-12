package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.Product;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    boolean existsById(@NonNull UUID id);
}