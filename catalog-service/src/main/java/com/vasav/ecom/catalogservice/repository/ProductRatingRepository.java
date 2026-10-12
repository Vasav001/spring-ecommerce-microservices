package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.dto.review.RatingSnapshot;
import com.vasav.ecom.catalogservice.entity.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProductRatingRepository extends JpaRepository<ProductRating, UUID> {

    // no @Modifying: RETURNING makes these result-producing queries (@Modifying only allows void/int/long)
    @Query(value = """
            INSERT INTO product_rating (product_id, rating_count, rating_sum, version, updated_at)
            VALUES (:productId, 1, :rating, 1, now())
            ON CONFLICT (product_id) DO UPDATE
               SET rating_count = product_rating.rating_count + 1,
                   rating_sum   = product_rating.rating_sum + :rating,
                   version      = product_rating.version + 1,
                   updated_at   = now()
            RETURNING product_id AS productId, rating_count AS ratingCount, rating_sum AS ratingSum, version
            """, nativeQuery = true)
    RatingSnapshot addRating(UUID productId, int rating);

    @Query(value = """
            UPDATE product_rating
               SET rating_count = rating_count + :countDelta,
                   rating_sum   = rating_sum + :sumDelta,
                   version      = version + 1,
                   updated_at   = now()
             WHERE product_id = :productId
            RETURNING product_id AS productId, rating_count AS ratingCount, rating_sum AS ratingSum, version
            """, nativeQuery = true)
    RatingSnapshot adjust(UUID productId, int countDelta, int sumDelta);
}