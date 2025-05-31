
package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.InventoryItemDTO;
import com.ecommerce.inventoryservice.dto.OrderCreatedEvent;

import java.util.List;

public interface InventoryService {
    InventoryItemDTO createOrUpdateInventory(InventoryItemDTO inventoryItemDTO);
    InventoryItemDTO getInventoryByProductId(Long productId);
    List<InventoryItemDTO> getAllInventory();
    boolean isProductAvailable(Long productId, Integer quantity);
    void reserveStock(Long productId, Integer quantity);
    void releaseStock(Long productId, Integer quantity);
    void processOrderCreatedEvent(OrderCreatedEvent event);
    List<InventoryItemDTO> getLowStockItems();
}
