package com.vasav.ecom.catalogservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "purchase")
@IdClass(Purchase.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class Purchase {

    @Id
    private String userId;

    @Id
    private UUID productId;

    private UUID orderId;

    @CreationTimestamp
    private Instant createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String userId;
        private UUID productId;
    }
}