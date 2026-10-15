package com.vasav.ecom.catalogservice.service;

import com.vasav.ecom.catalogservice.dto.ReviewRequest;
import com.vasav.ecom.catalogservice.dto.ReviewResponse;
import com.vasav.ecom.catalogservice.entity.Review;
import com.vasav.ecom.catalogservice.producer.RatingEventProducer;
import com.vasav.ecom.catalogservice.repository.ProductRepository;
import com.vasav.ecom.catalogservice.repository.PurchasedItemRepository;
import com.vasav.ecom.catalogservice.repository.ReviewRepository;
import com.vasav.ecom.catalogservice.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final PurchasedItemRepository purchasedItemRepository;
    private final RatingEventProducer ratingEvents;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> list(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable).map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public ReviewResponse mine(CurrentUser user, UUID productId) {
        return reviewRepository.findByProductIdAndCustomerId(productId, user.subject()).map(ReviewResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    @Transactional
    public ReviewResponse upsert(CurrentUser user, UUID productId, ReviewRequest request) {
        lockProduct(productId);
        Review review = reviewRepository.findByProductIdAndCustomerId(productId, user.subject()).orElse(null);
        if (review == null) {
            if (!purchasedItemRepository.existsByCustomerIdAndIdProductIdAndDeliveredTrue(user.subject(), productId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only delivered products can be reviewed");
            }
            review = new Review();
            review.setId(UUID.randomUUID());
            review.setProductId(productId);
            review.setCustomerId(user.subject());
            review.setAuthorName(user.displayName());
        }
        review.setRating(request.rating());
        review.setComment(request.comment());
        review = reviewRepository.saveAndFlush(review);
        publishRating(productId);
        return ReviewResponse.from(review);
    }

    @Transactional
    public void delete(CurrentUser user, UUID productId) {
        lockProduct(productId);
        Review review = reviewRepository.findByProductIdAndCustomerId(productId, user.subject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        reviewRepository.delete(review);
        reviewRepository.flush();
        publishRating(productId);
    }

    private void lockProduct(UUID productId) {
        productRepository.lockById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private void publishRating(UUID productId) {
        ratingEvents.changed(productId, reviewRepository.countByProductId(productId),
                reviewRepository.averageRating(productId));
    }
}
