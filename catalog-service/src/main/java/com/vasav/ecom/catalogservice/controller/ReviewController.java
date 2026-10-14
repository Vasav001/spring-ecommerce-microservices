package com.vasav.ecom.catalogservice.controller;

import com.vasav.ecom.catalogservice.dto.ReviewRequest;
import com.vasav.ecom.catalogservice.dto.ReviewResponse;
import com.vasav.ecom.catalogservice.security.CurrentUser;
import com.vasav.ecom.catalogservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/v1/reviews")
    public ResponseEntity<PagedModel<ReviewResponse>> list(@RequestParam UUID productId, Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(reviewService.list(productId, pageable)));
    }

    @PutMapping("/api/v1/products/{productId}/review")
    public ResponseEntity<ReviewResponse> upsert(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId,
                                                 @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.upsert(CurrentUser.from(jwt), productId, request));
    }

    @DeleteMapping("/api/v1/products/{productId}/review")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID productId) {
        reviewService.delete(CurrentUser.from(jwt), productId);
        return ResponseEntity.noContent().build();
    }
}
