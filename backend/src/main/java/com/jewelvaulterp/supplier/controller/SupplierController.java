package com.jewelvaulterp.supplier.controller;

import com.jewelvaulterp.supplier.dto.CreateSupplierRequest;
import com.jewelvaulterp.supplier.dto.SupplierResponse;
import com.jewelvaulterp.supplier.dto.UpdateSupplierRequest;
import com.jewelvaulterp.supplier.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public List<SupplierResponse> getSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @PostMapping
    public SupplierResponse createSupplier(
            @Valid @RequestBody CreateSupplierRequest request
    ) {
        return supplierService.createSupplier(request);
    }

    @PutMapping("/{id}")
    public SupplierResponse updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        return supplierService.updateSupplier(id, request);
    }

    @PatchMapping("/{id}/status")
    public SupplierResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return supplierService.updateStatus(id, active);
    }
}