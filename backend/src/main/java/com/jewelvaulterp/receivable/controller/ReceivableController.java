package com.jewelvaulterp.receivable.controller;

import com.jewelvaulterp.receivable.dto.CreateReceivableRequest;
import com.jewelvaulterp.receivable.dto.RecordReceivablePaymentRequest;
import com.jewelvaulterp.receivable.dto.ReceivableResponse;
import com.jewelvaulterp.receivable.entity.ReceivableStatus;
import com.jewelvaulterp.receivable.service.ReceivableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/receivables")
public class ReceivableController {

    private final ReceivableService receivableService;

    public ReceivableController(ReceivableService receivableService) {
        this.receivableService = receivableService;
    }

    @GetMapping
    public List<ReceivableResponse> getAll() {
        return receivableService.getAll();
    }

    @GetMapping("/{id}")
    public ReceivableResponse getById(@PathVariable UUID id) {
        return receivableService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<ReceivableResponse> getByCompany(
            @PathVariable UUID companyId
    ) {
        return receivableService.getByCompany(companyId);
    }

    @GetMapping("/customer/{customerId}")
    public List<ReceivableResponse> getByCustomer(
            @PathVariable UUID customerId
    ) {
        return receivableService.getByCustomer(customerId);
    }

    @GetMapping("/status/{status}")
    public List<ReceivableResponse> getByStatus(
            @PathVariable ReceivableStatus status
    ) {
        return receivableService.getByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceivableResponse create(
            @Valid @RequestBody CreateReceivableRequest request
    ) {
        return receivableService.create(request);
    }

    @PatchMapping("/{id}/payment")
    public ReceivableResponse recordPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RecordReceivablePaymentRequest request
    ) {
        return receivableService.recordPayment(id, request.amount());
    }

    @PatchMapping("/{id}/overdue")
    public ReceivableResponse markOverdue(
            @PathVariable UUID id
    ) {
        return receivableService.markOverdue(id);
    }

    @PatchMapping("/{id}/cancel")
    public ReceivableResponse cancel(
            @PathVariable UUID id
    ) {
        return receivableService.cancel(id);
    }
}