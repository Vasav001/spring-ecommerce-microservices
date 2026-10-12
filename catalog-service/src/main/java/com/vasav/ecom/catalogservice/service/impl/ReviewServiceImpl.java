package com.vasav.ecom.catalogservice.service.impl;

import com.vasav.ecom.catalogservice.dto.review.*;
import com.vasav.ecom.catalogservice.entity.Review;
import com.vasav.ecom.catalogservice.entity.enums.ProductStatus;
import com.vasav.ecom.catalogservice.entity.enums.ReviewStatus;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import com.vasav.ecom.catalogservice.exception.ProductNotFoundException;
import com.vasav.ecom.catalogservice.outbox.OutboxWriter;
import com.vasav.ecom.catalogservice.repository.ProductRatingRepository;
import com.vasav.ecom.catalogservice.repository.ProductRepository;
import com.vasav.ecom.catalogservice.repository.ReviewRepository;
import com.vasav.ecom.catalogservice.security.CurrentUser;
import com.vasav.ecom.catalogservice.service.PurchaseService;
import com.vasav.ecom.catalogservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final String RATING_CHANGED = "ProductRatingChanged";

    private final ReviewRepository reviews;
    private final ProductRatingRepository ratings;
    private final ProductRepository products;
    private final PurchaseService purchaseService;
    private final OutboxWriter outboxWriter;

    @Override
    @Transactional
    public ReviewResponse create(CurrentUser user, CreateReviewRequest request) {
        UUID productId = request.productId();
        requireActiveProduct(productId);
        if (!purchaseService.hasPurchased(user.subject(), productId)) {
            throw new RuntimeException();
        }
        if (reviews.existsByProductIdAndUserId(productId, user.subject())) {
            throw new RuntimeException();
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(user.subject());
        review.setAuthorName(user.displayName());
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setStatus(ReviewStatus.PUBLISHED);

        Review saved;
        try {
            saved = reviews.saveAndFlush(review);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException();
        }

        publish(ratings.addRating(productId, saved.getRating()));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse update(UUID reviewId, CurrentUser user, long expectedVersion, UpdateReviewRequest request) {
        Review review = findPublished(reviewId);
        requireAuthor(review, user);
        if (review.getVersion() != expectedVersion) {
            throw new RuntimeException();
        }

        int delta = request.rating() - review.getRating();
        review.setRating(request.rating());
        review.setComment(request.comment());
        Review saved = reviews.saveAndFlush(review);

        if (delta != 0) {
            publish(ratings.adjust(saved.getProductId(), 0, delta));
        }
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID reviewId, CurrentUser user) {
        Review review = reviews.findById(reviewId).orElseThrow(() -> new RuntimeException(String.valueOf(reviewId)));
        requireAuthor(review, user);
        hideIfPublished(review);
    }

    @Override
    @Transactional
    public void hide(UUID reviewId) {
        Review review = reviews.findById(reviewId).orElseThrow(() -> new RuntimeException(String.valueOf(reviewId)));
        hideIfPublished(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getByProduct(UUID productId, Pageable pageable) {
        return reviews.findByProductIdAndStatus(productId, ReviewStatus.PUBLISHED, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewEligibility canReview(CurrentUser user, UUID productId) {
        if (!purchaseService.hasPurchased(user.subject(), productId)) {
            return new ReviewEligibility(false, "NOT_PURCHASED");
        }
        if (reviews.existsByProductIdAndUserId(productId, user.subject())) {
            return new ReviewEligibility(false, "ALREADY_REVIEWED");
        }
        return new ReviewEligibility(true, null);
    }

    private void hideIfPublished(Review review) {
        if (review.getStatus() != ReviewStatus.PUBLISHED) {
            return;
        }
        review.setStatus(ReviewStatus.HIDDEN);
        reviews.saveAndFlush(review);
        publish(ratings.adjust(review.getProductId(), -1, -review.getRating()));
    }

    private Review findPublished(UUID reviewId) {
        return reviews.findById(reviewId)
                .filter(r -> r.getStatus() == ReviewStatus.PUBLISHED)
                .orElseThrow(() -> new RuntimeException(String.valueOf(reviewId)));
    }

    private void requireAuthor(Review review, CurrentUser user) {
        if (!review.getUserId().equals(user.subject())) {
            throw new RuntimeException();
        }
    }

    private void requireActiveProduct(UUID productId) {
        boolean active = products.findById(productId)
                .map(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElse(false);
        if (!active) {
            throw new ProductNotFoundException(productId);
        }
    }

    private void publish(RatingSnapshot r) {
        BigDecimal average = r.getRatingCount() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(r.getRatingSum())
                .divide(BigDecimal.valueOf(r.getRatingCount()), 2, RoundingMode.HALF_UP);
        outboxWriter.append(KafkaTopics.CATALOG_PRODUCT_RATINGS, r.getProductId().toString(), RATING_CHANGED,
                new ProductRatingChanged(r.getProductId(), r.getVersion(), r.getRatingCount(), average));
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(r.getId(), r.getProductId(), r.getAuthorName(), r.getRating(), r.getComment(),
                r.getVersion(), r.getCreatedAt(), r.getUpdatedAt());
    }
}