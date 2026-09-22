package com.jewelvaulterp.stocktransfer.controller;

import com.jewelvaulterp.stocktransfer.dto.CreateStockTransferRequest;
import com.jewelvaulterp.stocktransfer.dto.StockTransferResponse;
import com.jewelvaulterp.stocktransfer.entity.StockTransferStatus;
import com.jewelvaulterp.stocktransfer.service.StockTransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stock-transfers")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    public StockTransferController(
            StockTransferService stockTransferService
    ) {
        this.stockTransferService = stockTransferService;
    }

    @GetMapping
    public List<StockTransferResponse> getTransfers() {
        return stockTransferService.getAllTransfers();
    }

    @GetMapping("/{id}")
    public StockTransferResponse getTransfer(
            @PathVariable UUID id
    ) {
        return stockTransferService.getTransfer(id);
    }

    @PostMapping
    public StockTransferResponse createTransfer(
            @Valid @RequestBody CreateStockTransferRequest request
    ) {
        return stockTransferService.createTransfer(request);
    }

    @PatchMapping("/{id}/status")
    public StockTransferResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam StockTransferStatus status
    ) {
        return stockTransferService.updateStatus(id, status);
    }
}