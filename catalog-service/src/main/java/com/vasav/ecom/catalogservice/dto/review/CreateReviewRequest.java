package com.vasav.ecom.catalogservice.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReviewRequest(
        @NotNull UUID productId,
        @NotNull @Min(1) @Max(5) Short rating,
        @Size(max = 2000) String comment
) {
}