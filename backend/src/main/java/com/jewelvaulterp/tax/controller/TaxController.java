package com.jewelvaulterp.tax.controller;

import com.jewelvaulterp.tax.dto.CreateTaxRequest;
import com.jewelvaulterp.tax.dto.TaxResponse;
import com.jewelvaulterp.tax.entity.TaxType;
import com.jewelvaulterp.tax.service.TaxService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/taxes")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @GetMapping
    public List<TaxResponse> getAll() {
        return taxService.getAll();
    }

    @GetMapping("/{id}")
    public TaxResponse getById(@PathVariable UUID id) {
        return taxService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<TaxResponse> getByCompany(
            @PathVariable UUID companyId
    ) {
        return taxService.getByCompany(companyId);
    }

    @GetMapping("/company/{companyId}/active")
    public List<TaxResponse> getActiveByCompany(
            @PathVariable UUID companyId
    ) {
        return taxService.getActiveByCompany(companyId);
    }

    @GetMapping("/company/{companyId}/type/{taxType}")
    public List<TaxResponse> getByType(
            @PathVariable UUID companyId,
            @PathVariable TaxType taxType
    ) {
        return taxService.getByType(companyId, taxType);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaxResponse create(
            @Valid @RequestBody CreateTaxRequest request
    ) {
        return taxService.create(request);
    }

    @PatchMapping("/{id}/activate")
    public TaxResponse activate(@PathVariable UUID id) {
        return taxService.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    public TaxResponse deactivate(@PathVariable UUID id) {
        return taxService.deactivate(id);
    }
}