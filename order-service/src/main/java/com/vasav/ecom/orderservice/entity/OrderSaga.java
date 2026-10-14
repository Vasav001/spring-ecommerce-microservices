package com.vasav.ecom.orderservice.entity;

import com.vasav.ecom.orderservice.entity.enums.SagaStep;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_saga")
@Getter
@Setter
@NoArgsConstructor
public class OrderSaga {

    @Id
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private SagaStep step;

    private Instant stepDeadline;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
