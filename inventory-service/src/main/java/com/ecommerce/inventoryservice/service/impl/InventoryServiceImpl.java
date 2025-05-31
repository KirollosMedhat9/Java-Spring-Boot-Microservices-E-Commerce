
package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.dto.InventoryItemDTO;
import com.ecommerce.inventoryservice.dto.OrderCreatedEvent;
import com.ecommerce.inventoryservice.model.InventoryItem;
import com.ecommerce.inventoryservice.repository.InventoryItemRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    
    private final InventoryItemRepository inventoryItemRepository;
    
    @Override
    @Transactional
    public InventoryItemDTO createOrUpdateInventory(InventoryItemDTO inventoryItemDTO) {
        InventoryItem item = inventoryItemRepository.findByProductId(inventoryItemDTO.getProductId())
                .orElse(new InventoryItem());
        
        if (item.getId() == null) {
            item.setProductId(inventoryItemDTO.getProductId());
            item.setReservedQuantity(0);
        }
        
        item.setQuantity(inventoryItemDTO.getQuantity());
        
        InventoryItem savedItem = inventoryItemRepository.save(item);
        return mapToDTO(savedItem);
    }
    
    @Override
    public InventoryItemDTO getInventoryByProductId(Long productId) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        return mapToDTO(item);
    }
    
    @Override
    public List<InventoryItemDTO> getAllInventory() {
        return inventoryItemRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isProductAvailable(Long productId, Integer quantity) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElse(null);
        
        if (item == null) {
            return false;
        }
        
        int availableQuantity = item.getQuantity() - item.getReservedQuantity();
        return availableQuantity >= quantity;
    }
    
    @Override
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory: " + productId));
        
        int availableQuantity = item.getQuantity() - item.getReservedQuantity();
        if (availableQuantity < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }
        
        item.setReservedQuantity(item.getReservedQuantity() + quantity);
        inventoryItemRepository.save(item);
        
        log.info("Reserved {} units for product {}", quantity, productId);
    }
    
    @Override
    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory: " + productId));
        
        item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - quantity));
        inventoryItemRepository.save(item);
        
        log.info("Released {} units for product {}", quantity, productId);
    }
    
    @Override
    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    @Transactional
    public void processOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Processing order created event for order: {}", event.getOrderId());
        
        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
            try {
                reserveStock(item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to reserve stock for product {} in order {}: {}", 
                         item.getProductId(), event.getOrderId(), e.getMessage());
                // In a production system, you might want to publish a compensation event
            }
        }
    }
    
    @Override
    public List<InventoryItemDTO> getLowStockItems() {
        return inventoryItemRepository.findLowStockItems().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    private InventoryItemDTO mapToDTO(InventoryItem item) {
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setReservedQuantity(item.getReservedQuantity());
        dto.setAvailableQuantity(item.getQuantity() - item.getReservedQuantity());
        return dto;
    }
}
