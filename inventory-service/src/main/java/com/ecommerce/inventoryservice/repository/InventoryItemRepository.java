
package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByProductId(Long productId);
    
    @Query("SELECT i FROM InventoryItem i WHERE i.productId IN :productIds")
    List<InventoryItem> findByProductIdIn(List<Long> productIds);
    
    @Query("SELECT i FROM InventoryItem i WHERE (i.quantity - i.reservedQuantity) < 10")
    List<InventoryItem> findLowStockItems();
}
