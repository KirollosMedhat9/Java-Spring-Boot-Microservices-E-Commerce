
package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.InventoryItemDTO;
import com.ecommerce.orderservice.dto.OrderDTO;
import com.ecommerce.productservice.dto.ProductDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventoryService {

    Long extractProductId(ProductDTO event);

    InventoryItemDTO createOrUpdateInventory(InventoryItemDTO inventoryItemDTO);

    InventoryItemDTO createInventoryItem(InventoryItemDTO itemDTO);

    InventoryItemDTO getInventoryItemById(Long id);

    InventoryItemDTO getInventoryByProductId(Long productId);

    List<InventoryItemDTO> getAllInventory();

    boolean isProductAvailable(Long productId, Integer quantity);

    void reserveStock(Long productId, Integer quantity);

    void releaseStock(Long productId, Integer quantity);

    void handleOrderCreated(OrderDTO event);

//    @KafkaListener(topics = "order.created", groupId = "inventory-service")
//    @Transactional
//    void processOrderDTO(OrderDTO event);

    List<InventoryItemDTO> getLowStockItems();

    List<InventoryItemDTO> getAllInventoryItems();

    @Transactional
    InventoryItemDTO updateStock(Long productId, Integer quantity);

    boolean isInStock(Long productId, Integer requiredQuantity);


}
