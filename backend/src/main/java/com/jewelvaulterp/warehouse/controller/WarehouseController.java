package com.jewelvaulterp.warehouse.controller;

import com.jewelvaulterp.warehouse.dto.CreateWarehouseRequest;
import com.jewelvaulterp.warehouse.dto.UpdateWarehouseRequest;
import com.jewelvaulterp.warehouse.dto.WarehouseResponse;
import com.jewelvaulterp.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public List<WarehouseResponse> getWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    @PostMapping
    public WarehouseResponse createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request
    ) {
        return warehouseService.createWarehouse(request);
    }

    @PutMapping("/{id}")
    public WarehouseResponse updateWarehouse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWarehouseRequest request
    ) {
        return warehouseService.updateWarehouse(id, request);
    }

    @PatchMapping("/{id}/status")
    public WarehouseResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return warehouseService.updateStatus(id, active);
    }
}