package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.Review;
import com.vasav.ecom.catalogservice.entity.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductIdAndStatus(UUID productId, ReviewStatus status, Pageable pageable);

    boolean existsByProductIdAndUserId(UUID productId, String userId);
}