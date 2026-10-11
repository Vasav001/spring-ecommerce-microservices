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
@Table(name = "processed_event")
@IdClass(ProcessedEvent.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class ProcessedEvent {

    @Id
    private String consumer;

    @Id
    private UUID eventId;

    @CreationTimestamp
    private Instant processedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String consumer;
        private UUID eventId;
    }
}