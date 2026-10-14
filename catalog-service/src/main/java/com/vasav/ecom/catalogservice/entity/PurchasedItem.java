package com.vasav.ecom.catalogservice.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "purchased_item")
@Getter
@Setter
@NoArgsConstructor
public class PurchasedItem {

    @EmbeddedId
    private Key id;

    private String customerId;

    private boolean delivered;

    @Embeddable
    public record Key(UUID orderId, UUID productId) implements Serializable {
    }
}
