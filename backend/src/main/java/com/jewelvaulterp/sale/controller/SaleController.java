package com.jewelvaulterp.sale.controller;

import com.jewelvaulterp.sale.dto.CreateSaleRequest;
import com.jewelvaulterp.sale.dto.SaleResponse;
import com.jewelvaulterp.sale.entity.SaleStatus;
import com.jewelvaulterp.sale.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public List<SaleResponse> getSales() {
        return saleService.getAllSales();
    }

    @GetMapping("/{id}")
    public SaleResponse getSale(
            @PathVariable UUID id
    ) {
        return saleService.getSale(id);
    }

    @PostMapping
    public SaleResponse createSale(
            @Valid @RequestBody CreateSaleRequest request
    ) {
        return saleService.createSale(request);
    }

    @PatchMapping("/{id}/status")
    public SaleResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam SaleStatus status
    ) {
        return saleService.updateStatus(id, status);
    }
}