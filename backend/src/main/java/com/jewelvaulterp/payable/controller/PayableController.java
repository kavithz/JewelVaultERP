package com.jewelvaulterp.payable.controller;

import com.jewelvaulterp.payable.dto.CreatePayableRequest;
import com.jewelvaulterp.payable.dto.PayableResponse;
import com.jewelvaulterp.payable.dto.RecordPayablePaymentRequest;
import com.jewelvaulterp.payable.entity.PayableStatus;
import com.jewelvaulterp.payable.service.PayableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payables")
public class PayableController {

    private final PayableService payableService;

    public PayableController(PayableService payableService) {
        this.payableService = payableService;
    }

    @GetMapping
    public List<PayableResponse> getAll() {
        return payableService.getAll();
    }

    @GetMapping("/{id}")
    public PayableResponse getById(@PathVariable UUID id) {
        return payableService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<PayableResponse> getByCompany(
            @PathVariable UUID companyId
    ) {
        return payableService.getByCompany(companyId);
    }

    @GetMapping("/supplier/{supplierId}")
    public List<PayableResponse> getBySupplier(
            @PathVariable UUID supplierId
    ) {
        return payableService.getBySupplier(supplierId);
    }

    @GetMapping("/status/{status}")
    public List<PayableResponse> getByStatus(
            @PathVariable PayableStatus status
    ) {
        return payableService.getByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PayableResponse create(
            @Valid @RequestBody CreatePayableRequest request
    ) {
        return payableService.create(request);
    }

    @PatchMapping("/{id}/payment")
    public PayableResponse recordPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RecordPayablePaymentRequest request
    ) {
        return payableService.recordPayment(id, request.amount());
    }

    @PatchMapping("/{id}/overdue")
    public PayableResponse markOverdue(
            @PathVariable UUID id
    ) {
        return payableService.markOverdue(id);
    }

    @PatchMapping("/{id}/cancel")
    public PayableResponse cancel(
            @PathVariable UUID id
    ) {
        return payableService.cancel(id);
    }
}