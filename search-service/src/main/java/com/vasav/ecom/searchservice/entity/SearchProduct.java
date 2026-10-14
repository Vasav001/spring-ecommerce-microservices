package com.vasav.ecom.searchservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_product")
@Getter
@Setter
@NoArgsConstructor
public class SearchProduct {

    @Id
    private UUID productId;

    private String name;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private String status;

    private boolean inStock;

    private BigDecimal averageRating;

    private long reviewCount;

    private Instant updatedAt;
}
