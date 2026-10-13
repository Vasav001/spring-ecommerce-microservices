package com.vasav.ecom.inventoryservice.repository;

import com.vasav.ecom.inventoryservice.entity.StockItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.productId in :productIds order by s.productId")
    List<StockItem> lockAll(@Param("productIds") Collection<UUID> productIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update StockItem s set s.reserved = s.reserved + :qty
            where s.productId = :productId and s.onHand - s.reserved >= :qty""")
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
    @Query(value = """
            INSERT INTO stock_item (product_id, on_hand) VALUES (:productId, :qty)
            ON CONFLICT (product_id) DO UPDATE SET on_hand = stock_item.on_hand + EXCLUDED.on_hand""", nativeQuery = true)
    int restock(@Param("productId") UUID productId, @Param("qty") int qty);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update StockItem s set s.onHand = s.onHand + :delta
            where s.productId = :productId and s.onHand + :delta >= s.reserved""")
    int adjust(@Param("productId") UUID productId, @Param("delta") int delta);
}
