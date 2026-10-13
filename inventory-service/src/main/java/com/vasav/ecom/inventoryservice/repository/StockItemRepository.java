package com.vasav.ecom.inventoryservice.repository;

import com.vasav.ecom.inventoryservice.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update StockItem s set s.reserved = s.reserved + :qty
            where s.productId = :productId and s.active = true and s.onHand - s.reserved >= :qty""")
    int reserve(@Param("productId") UUID productId, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update StockItem s set s.reserved = s.reserved - :qty
            where s.productId = :productId and s.reserved >= :qty""")
    int release(@Param("productId") UUID productId, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update StockItem s set s.onHand = s.onHand - :qty, s.reserved = s.reserved - :qty
            where s.productId = :productId and s.reserved >= :qty""")
    int confirm(@Param("productId") UUID productId, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update StockItem s set s.onHand = s.onHand + :delta
            where s.productId = :productId and s.onHand + :delta >= s.reserved""")
    int adjust(@Param("productId") UUID productId, @Param("delta") int delta);
}
