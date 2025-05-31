
package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.InventoryItemDTO;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @PostMapping
    public ResponseEntity<InventoryItemDTO> createOrUpdateInventory(@RequestBody InventoryItemDTO inventoryItemDTO) {
        InventoryItemDTO created = inventoryService.createOrUpdateInventory(inventoryItemDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryItemDTO> getInventoryByProductId(@PathVariable Long productId) {
        InventoryItemDTO inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping
    public ResponseEntity<List<InventoryItemDTO>> getAllInventory() {
        List<InventoryItemDTO> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }
    
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkAvailability(@PathVariable Long productId, @RequestParam Integer quantity) {
        boolean available = inventoryService.isProductAvailable(productId, quantity);
        return ResponseEntity.ok(available);
    }
    
    @PostMapping("/reserve/{productId}")
    public ResponseEntity<Void> reserveStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/release/{productId}")
    public ResponseEntity<Void> releaseStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        inventoryService.releaseStock(productId, quantity);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemDTO>> getLowStockItems() {
        List<InventoryItemDTO> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }
}
