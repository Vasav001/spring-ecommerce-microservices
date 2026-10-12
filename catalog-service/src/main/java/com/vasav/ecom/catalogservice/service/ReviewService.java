package com.vasav.ecom.catalogservice.service;

import com.vasav.ecom.catalogservice.dto.review.CreateReviewRequest;
import com.vasav.ecom.catalogservice.dto.review.ReviewEligibility;
import com.vasav.ecom.catalogservice.dto.review.ReviewResponse;
import com.vasav.ecom.catalogservice.dto.review.UpdateReviewRequest;
import com.vasav.ecom.catalogservice.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse create(CurrentUser user, CreateReviewRequest request);

    ReviewResponse update(UUID reviewId, CurrentUser user, long expectedVersion, UpdateReviewRequest request);

    void delete(UUID reviewId, CurrentUser user);

    void hide(UUID reviewId);

    Page<ReviewResponse> getByProduct(UUID productId, Pageable pageable);

    ReviewEligibility canReview(CurrentUser user, UUID productId);
}