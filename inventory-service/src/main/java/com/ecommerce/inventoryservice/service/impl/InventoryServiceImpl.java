package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.dto.InventoryItemDTO;
import com.ecommerce.orderservice.dto.OrderDTO;
import com.ecommerce.orderservice.dto.OrderItemDTO;
import com.ecommerce.productservice.dto.ProductDTO;
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

    private final InventoryItemRepository inventoryRepository;

    @Override
    public InventoryItemDTO createInventoryItem(InventoryItemDTO itemDTO) {
        InventoryItem item = mapToEntity(itemDTO);
        InventoryItem savedItem = inventoryRepository.save(item);
        log.info("Inventory item created with ID: {}", savedItem.getId());
        return mapToDTO(savedItem);
    }

    @Override
    public InventoryItemDTO getInventoryItemById(Long id) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with ID: " + id));
        return mapToDTO(item);
    }

    @Override
    public InventoryItemDTO getInventoryByProductId(Long productId) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + productId));
        return mapToDTO(item);
    }

    @Override
    public List<InventoryItemDTO> getAllInventoryItems() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public InventoryItemDTO updateStock(Long productId, Integer quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + productId));

        item.setStockQuantity(quantity);
        InventoryItem updatedItem = inventoryRepository.save(item);
        log.info("Stock updated for product ID: {} to quantity: {}", productId, quantity);

        return mapToDTO(updatedItem);
    }

    @Override
    public boolean isInStock(Long productId, Integer requiredQuantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElse(null);

        if (item == null) {
            return false;
        }

        return item.getStockQuantity() >= requiredQuantity;
    }

    // Kafka Consumers
    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void handleOrderCreated(OrderDTO orderEvent) {
        log.info("Received order created event for order ID: {}", orderEvent.getId());

        // Process each order item to update inventory
        orderEvent.getItems().forEach(orderItem -> {
            try {
                InventoryItem inventoryItem = inventoryRepository.findByProductId(orderItem.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found in inventory: " + orderItem.getProductId()));

                if (inventoryItem.getStockQuantity() >= orderItem.getQuantity()) {
                    inventoryItem.setStockQuantity(inventoryItem.getStockQuantity() - orderItem.getQuantity());
                    inventoryRepository.save(inventoryItem);
                    log.info("Reduced stock for product {} by {}", orderItem.getProductId(), orderItem.getQuantity());
                } else {
                    log.error("Insufficient stock for product {}: required {}, available {}",
                            orderItem.getProductId(), orderItem.getQuantity(), inventoryItem.getStockQuantity());
                }
            } catch (Exception e) {
                log.error("Error processing order item for product {}: {}", orderItem.getProductId(), e.getMessage());
            }
        });
    }

    @KafkaListener(topics = "product.created", groupId = "inventory-service")
    public void handleProductCreated(ProductDTO productDTO) {
        log.info("Received product created event: {}", productDTO);

        // Parse the product event and create inventory entry
        try {
            Long productId = extractProductId(productDTO);
            Integer initialStock = extractInitialStock(productDTO);

            // Create inventory item for new product
            InventoryItem newInventoryItem = new InventoryItem();
            newInventoryItem.setProductId(productId);
            newInventoryItem.setQuantity(productDTO.getStockQuantity());
            newInventoryItem.setStockQuantity(initialStock != null ? initialStock : 0);
            newInventoryItem.setReservedQuantity(0);

            inventoryRepository.save(newInventoryItem);
            createOrUpdateInventory(mapToDTO(newInventoryItem));
            log.info("Created inventory entry for new product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error creating inventory for new product: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "product.deleted", groupId = "inventory-service")
    public void handleProductDeleted(ProductDTO productDTO) {
        log.info("Received product deleted event: {}", productDTO);

        try {
            Long productId = extractProductId(productDTO);
            inventoryRepository.findByProductId(productId).ifPresent(inventoryItem -> {
                inventoryRepository.delete(inventoryItem);
                log.info("Removed inventory entry for deleted product ID: {}", productId);
            });
        } catch (Exception e) {
            log.error("Error deleting inventory for product: {}", e.getMessage());
        }
    }


    @Override
    public Long extractProductId(ProductDTO event) {
        String eventStr = event.toString();
        return event.getId();
    }

    private Integer extractInitialStock(ProductDTO event) {
        return event.getStockQuantity();
    }

    private InventoryItemDTO mapToDTO(InventoryItem item) {
        return new InventoryItemDTO(
                item.getId(),
                item.getProductId(),
                item.getStockQuantity(),
                item.getReservedQuantity(),
                item.getStockQuantity()
        );
    }

    private InventoryItem mapToEntity(InventoryItemDTO itemDTO) {
        InventoryItem item = new InventoryItem();
        item.setId(itemDTO.getId());
        item.setProductId(itemDTO.getProductId());
        item.setStockQuantity(itemDTO.getAvailableQuantity());
        item.setReservedQuantity(itemDTO.getReservedQuantity());
        return item;
    }


    @Override
    @Transactional
    public InventoryItemDTO createOrUpdateInventory(InventoryItemDTO inventoryItemDTO) {
        InventoryItem item = inventoryRepository.findByProductId(inventoryItemDTO.getProductId())
                .orElse(new InventoryItem());

        if (item.getId() == null) {
            item.setProductId(inventoryItemDTO.getProductId());
            item.setReservedQuantity(0);
        }

        item.setQuantity(inventoryItemDTO.getQuantity());

        InventoryItem savedItem = inventoryRepository.save(item);
        return mapToDTO(savedItem);
    }

    @Override
    public List<InventoryItemDTO> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isProductAvailable(Long productId, Integer quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
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
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory: " + productId));

        int availableQuantity = item.getQuantity() - item.getReservedQuantity();
        if (availableQuantity < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }

        item.setReservedQuantity(item.getReservedQuantity() + quantity);
        inventoryRepository.save(item);

        log.info("Reserved {} units for product {}", quantity, productId);
    }

    @Override
    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        InventoryItem item = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory: " + productId));

        item.setReservedQuantity(Math.max(0, item.getReservedQuantity() - quantity));
        inventoryRepository.save(item);

        log.info("Released {} units for product {}", quantity, productId);
    }

    @Override
    public List<InventoryItemDTO> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

}