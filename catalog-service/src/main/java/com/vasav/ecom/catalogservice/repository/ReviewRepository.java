package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);

    Optional<Review> findByProductIdAndCustomerId(UUID productId, String customerId);

    long countByProductId(UUID productId);

    @Query("select avg(r.rating) from Review r where r.productId = :productId")
    Double averageRating(@Param("productId") UUID productId);
}
