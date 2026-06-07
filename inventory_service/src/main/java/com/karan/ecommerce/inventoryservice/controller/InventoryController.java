package com.karan.ecommerce.inventoryservice.controller;

import com.karan.ecommerce.inventoryservice.dto.InventoryAvailabilityResponse;
import com.karan.ecommerce.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}/availability")
    public ResponseEntity<InventoryAvailabilityResponse> getAvailability(@PathVariable Long productId,
                                                                         @RequestParam(required = false) String warehouseCode) {
        return ResponseEntity.ok(inventoryService.getAvailability(productId, warehouseCode));
    }
}
