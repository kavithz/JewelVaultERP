package com.jewelvaulterp.stockmovement.controller;

import com.jewelvaulterp.stockmovement.dto.CreateStockMovementRequest;
import com.jewelvaulterp.stockmovement.dto.StockMovementResponse;
import com.jewelvaulterp.stockmovement.service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(
            StockMovementService stockMovementService
    ) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    public List<StockMovementResponse> getMovements() {
        return stockMovementService.getAllMovements();
    }

    @GetMapping("/{id}")
    public StockMovementResponse getMovement(
            @PathVariable UUID id
    ) {
        return stockMovementService.getMovement(id);
    }

    @PostMapping
    public StockMovementResponse createMovement(
            @Valid @RequestBody CreateStockMovementRequest request
    ) {
        return stockMovementService.createMovement(request);
    }
}