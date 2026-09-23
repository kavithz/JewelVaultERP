package com.jewelvaulterp.stockadjustment.controller;

import com.jewelvaulterp.stockadjustment.dto.CreateStockAdjustmentRequest;
import com.jewelvaulterp.stockadjustment.dto.StockAdjustmentResponse;
import com.jewelvaulterp.stockadjustment.entity.StockAdjustmentStatus;
import com.jewelvaulterp.stockadjustment.service.StockAdjustmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-adjustments")
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;

    public StockAdjustmentController(
            StockAdjustmentService stockAdjustmentService
    ) {
        this.stockAdjustmentService = stockAdjustmentService;
    }

    @GetMapping
    public List<StockAdjustmentResponse> getAdjustments() {
        return stockAdjustmentService.getAllAdjustments();
    }

    @GetMapping("/{id}")
    public StockAdjustmentResponse getAdjustment(
            @PathVariable UUID id
    ) {
        return stockAdjustmentService.getAdjustment(id);
    }

    @PostMapping
    public StockAdjustmentResponse createAdjustment(
            @Valid @RequestBody CreateStockAdjustmentRequest request
    ) {
        return stockAdjustmentService.createAdjustment(request);
    }

    @PatchMapping("/{id}/status")
    public StockAdjustmentResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam StockAdjustmentStatus status
    ) {
        return stockAdjustmentService.updateStatus(id, status);
    }
}