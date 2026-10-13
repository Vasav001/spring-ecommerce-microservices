package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    @Modifying
    @Query(value = "INSERT INTO processed_event (event_id, consumer) VALUES (:eventId, :consumer) ON CONFLICT (event_id) DO NOTHING",
            nativeQuery = true)
    int markProcessed(@Param("eventId") UUID eventId, @Param("consumer") String consumer);
}