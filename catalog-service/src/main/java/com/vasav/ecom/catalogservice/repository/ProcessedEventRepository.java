package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, ProcessedEvent.Key> {

    @Modifying
    @Query(value = """
            INSERT INTO processed_event (consumer, event_id) VALUES (:consumer, :eventId)
            ON CONFLICT DO NOTHING""", nativeQuery = true)
    int markProcessed(String consumer, UUID eventId);    // 1 = first time, 0 = duplicate
}