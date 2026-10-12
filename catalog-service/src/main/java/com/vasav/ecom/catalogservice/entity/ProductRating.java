package com.vasav.ecom.catalogservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_rating")
@Getter
@Setter
@NoArgsConstructor
public class ProductRating {

    @Id
    private UUID productId;

    @Column(nullable = false)
    private int ratingCount;

    @Column(nullable = false)
    private int ratingSum;

    @Column(nullable = false)
    private long version;

    @Column(nullable = false)
    private Instant updatedAt;
}