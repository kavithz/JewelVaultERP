package com.jewelvaulterp.inventory.controller;

import com.jewelvaulterp.inventory.dto.CreateInventoryRequest;
import com.jewelvaulterp.inventory.dto.InventoryResponse;
import com.jewelvaulterp.inventory.dto.UpdateInventoryRequest;
import com.jewelvaulterp.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> getInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{id}")
    public InventoryResponse getInventory(
            @PathVariable UUID id
    ) {
        return inventoryService.getInventory(id);
    }

    @PostMapping
    public InventoryResponse createInventory(
            @Valid @RequestBody CreateInventoryRequest request
    ) {
        return inventoryService.createInventory(request);
    }

    @PutMapping("/{id}")
    public InventoryResponse updateInventory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInventoryRequest request
    ) {
        return inventoryService.updateInventory(id, request);
    }
}