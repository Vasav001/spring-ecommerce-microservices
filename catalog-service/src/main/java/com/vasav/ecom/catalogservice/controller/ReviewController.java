package com.vasav.ecom.catalogservice.controller;

import com.vasav.ecom.catalogservice.config.ETags;
import com.vasav.ecom.catalogservice.dto.review.CreateReviewRequest;
import com.vasav.ecom.catalogservice.dto.review.ReviewEligibility;
import com.vasav.ecom.catalogservice.dto.review.ReviewResponse;
import com.vasav.ecom.catalogservice.dto.review.UpdateReviewRequest;
import com.vasav.ecom.catalogservice.security.CurrentUser;
import com.vasav.ecom.catalogservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public PagedModel<ReviewResponse> getByProduct(
            @RequestParam UUID productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(reviewService.getByProduct(productId, pageable));
    }

    @GetMapping("/eligibility")
    public ReviewEligibility eligibility(@AuthenticationPrincipal Jwt jwt, @RequestParam UUID productId) {
        return reviewService.canReview(CurrentUser.from(jwt), productId);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse created = reviewService.create(CurrentUser.from(jwt), request);
        return ResponseEntity.created(URI.create("/api/v1/reviews/" + created.id()))
                .eTag(ETags.of(created.version()))
                .body(created);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                 @PathVariable UUID reviewId,
                                                 @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                 @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse updated = reviewService.update(reviewId, CurrentUser.from(jwt), ETags.parse(ifMatch), request);
        return ResponseEntity.ok().eTag(ETags.of(updated.version())).body(updated);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID reviewId) {
        reviewService.delete(reviewId, CurrentUser.from(jwt));
        return ResponseEntity.noContent().build();
    }
}