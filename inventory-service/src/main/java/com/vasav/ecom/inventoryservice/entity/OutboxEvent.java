package com.vasav.ecom.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(insertable = false, updatable = false)
    private Long seq;

    private String aggregateId;
    private String topic;
    private String eventType;
    private int eventVersion = 1;
    private String correlationId;

    @Column(columnDefinition = "text")
    private String payload;

    @CreationTimestamp
    private Instant createdAt;

    private Instant publishedAt;
}