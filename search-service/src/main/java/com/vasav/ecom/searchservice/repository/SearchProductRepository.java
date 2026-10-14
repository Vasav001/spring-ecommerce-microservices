package com.vasav.ecom.searchservice.repository;

import com.vasav.ecom.searchservice.entity.SearchProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface SearchProductRepository extends JpaRepository<SearchProduct, UUID>, JpaSpecificationExecutor<SearchProduct> {

    @Modifying
    @Query(value = """
            INSERT INTO search_product (product_id, name, description, price, image_url, status)
            VALUES (:productId, :name, :description, :price, :imageUrl, :status)
            ON CONFLICT (product_id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description,
                price = EXCLUDED.price, image_url = EXCLUDED.image_url, status = EXCLUDED.status, updated_at = now()""",
            nativeQuery = true)
    int upsertProduct(@Param("productId") UUID productId, @Param("name") String name,
                      @Param("description") String description, @Param("price") BigDecimal price,
                      @Param("imageUrl") String imageUrl, @Param("status") String status);

    @Modifying
    @Query(value = """
            INSERT INTO search_product (product_id, in_stock) VALUES (:productId, :inStock)
            ON CONFLICT (product_id) DO UPDATE SET in_stock = EXCLUDED.in_stock, updated_at = now()""",
            nativeQuery = true)
    int upsertStock(@Param("productId") UUID productId, @Param("inStock") boolean inStock);

    @Modifying
    @Query(value = """
            INSERT INTO search_product (product_id, average_rating, review_count)
            VALUES (:productId, :averageRating, :reviewCount)
            ON CONFLICT (product_id) DO UPDATE SET average_rating = EXCLUDED.average_rating,
                review_count = EXCLUDED.review_count, updated_at = now()""",
            nativeQuery = true)
    int upsertRating(@Param("productId") UUID productId, @Param("averageRating") BigDecimal averageRating,
                     @Param("reviewCount") long reviewCount);
}
