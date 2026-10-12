package com.vasav.ecom.catalogservice.controller;

import com.vasav.ecom.catalogservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{reviewId}/hide")
    public ResponseEntity<Void> hide(@PathVariable UUID reviewId) {
        reviewService.hide(reviewId);
        return ResponseEntity.noContent().build();
    }
}