package com.jewelvaulterp.integration.controller;

import com.jewelvaulterp.integration.dto.*;
import com.jewelvaulterp.integration.service.IntegrationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/health")
    public ResponseEntity<IntegrationHealthResponse> health() {
        return ResponseEntity.ok(integrationService.getHealth());
    }

    @GetMapping("/{companyId}/products")
    public ResponseEntity<IntegrationPageResponse<IntegrationProductResponse>> products(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getProducts(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/customers")
    public ResponseEntity<IntegrationPageResponse<IntegrationCustomerResponse>> customers(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getCustomers(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/suppliers")
    public ResponseEntity<IntegrationPageResponse<IntegrationSupplierResponse>> suppliers(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getSuppliers(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/employees")
    public ResponseEntity<IntegrationPageResponse<IntegrationEmployeeResponse>> employees(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getEmployees(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/accounts")
    public ResponseEntity<IntegrationPageResponse<IntegrationAccountResponse>> accounts(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getAccounts(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/taxes")
    public ResponseEntity<IntegrationPageResponse<IntegrationTaxResponse>> taxes(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getTaxes(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/warehouses")
    public ResponseEntity<IntegrationPageResponse<IntegrationWarehouseResponse>> warehouses(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getWarehouses(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/branches")
    public ResponseEntity<IntegrationPageResponse<IntegrationBranchResponse>> branches(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getBranches(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/sales")
    public ResponseEntity<IntegrationPageResponse<IntegrationSaleResponse>> sales(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getSales(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/purchases")
    public ResponseEntity<IntegrationPageResponse<IntegrationPurchaseResponse>> purchases(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getPurchases(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/payments")
    public ResponseEntity<IntegrationPageResponse<IntegrationPaymentResponse>> payments(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getPayments(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/invoices")
    public ResponseEntity<IntegrationPageResponse<IntegrationInvoiceResponse>> invoices(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getInvoices(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/payroll")
    public ResponseEntity<IntegrationPageResponse<IntegrationPayrollResponse>> payroll(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getPayroll(companyId, updatedSince, page, size));
    }

    @GetMapping("/{companyId}/audit-logs")
    public ResponseEntity<IntegrationPageResponse<IntegrationAuditLogResponse>> auditLogs(
            @PathVariable UUID companyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedSince,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return ResponseEntity.ok(integrationService.getAuditLogs(companyId, updatedSince, page, size));
    }
}
