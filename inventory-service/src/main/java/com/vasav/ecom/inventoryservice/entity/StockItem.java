package com.vasav.ecom.inventoryservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stock_item")
@Getter
@Setter
@NoArgsConstructor
public class StockItem {

    @Id
    private UUID productId;

    private boolean active = true;
    private int onHand;
    private int reserved;

    public StockItem(UUID productId) {
        this.productId = productId;
    }

    public int available() {
        return onHand - reserved;
    }
}
