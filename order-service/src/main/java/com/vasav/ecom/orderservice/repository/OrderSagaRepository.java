package com.vasav.ecom.orderservice.repository;

import com.vasav.ecom.orderservice.entity.OrderSaga;
import com.vasav.ecom.orderservice.entity.enums.SagaStep;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, UUID> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update OrderSaga s set s.step = :to, s.stepDeadline = :deadline, s.updatedAt = instant
            where s.orderId = :orderId and s.step = :from""")
    int advance(@Param("orderId") UUID orderId, @Param("from") SagaStep from,
                @Param("to") SagaStep to, @Param("deadline") Instant deadline);

    @Query("""
            select s.orderId from OrderSaga s
            where s.step is not null and s.stepDeadline < :now
            order by s.stepDeadline""")
    List<UUID> findOrderIdsByDeadlineBefore(@Param("now") Instant now, Limit limit);
}
