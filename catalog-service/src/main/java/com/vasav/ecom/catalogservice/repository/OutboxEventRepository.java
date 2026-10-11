package com.vasav.ecom.catalogservice.repository;

import com.vasav.ecom.catalogservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = "SELECT * FROM outbox_event WHERE published_at IS NULL ORDER BY seq LIMIT :limit",
            nativeQuery = true)
    List<OutboxEvent> findPending(int limit);

    @Query(value = "SELECT pg_try_advisory_xact_lock(hashtext('outbox-poller'))", nativeQuery = true)
    boolean tryLock();
}