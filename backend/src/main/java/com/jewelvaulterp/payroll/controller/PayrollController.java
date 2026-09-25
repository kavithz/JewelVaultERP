package com.jewelvaulterp.payroll.controller;

import com.jewelvaulterp.payroll.dto.CreatePayrollRequest;
import com.jewelvaulterp.payroll.dto.PayrollResponse;
import com.jewelvaulterp.payroll.entity.PayrollPaymentMethod;
import com.jewelvaulterp.payroll.entity.PayrollStatus;
import com.jewelvaulterp.payroll.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    public List<PayrollResponse> getAll() {
        return payrollService.getAll();
    }

    @GetMapping("/{id}")
    public PayrollResponse getById(
            @PathVariable UUID id
    ) {
        return payrollService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<PayrollResponse> getByCompany(
            @PathVariable UUID companyId
    ) {
        return payrollService.getByCompany(companyId);
    }

    @GetMapping("/employee/{employeeId}")
    public List<PayrollResponse> getByEmployee(
            @PathVariable UUID employeeId
    ) {
        return payrollService.getByEmployee(employeeId);
    }

    @GetMapping("/status/{status}")
    public List<PayrollResponse> getByStatus(
            @PathVariable PayrollStatus status
    ) {
        return payrollService.getByStatus(status);
    }

    @GetMapping("/period")
    public List<PayrollResponse> getByPeriod(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return payrollService.getByPeriod(year, month);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollResponse create(
            @Valid @RequestBody CreatePayrollRequest request
    ) {
        return payrollService.create(request);
    }

    @PatchMapping("/{id}/process")
    public PayrollResponse process(
            @PathVariable UUID id
    ) {
        return payrollService.process(id);
    }

    @PatchMapping("/{id}/pay")
    public PayrollResponse markPaid(
            @PathVariable UUID id,
            @RequestParam LocalDate paymentDate,
            @RequestParam PayrollPaymentMethod paymentMethod,
            @RequestParam(required = false) String referenceNumber
    ) {
        return payrollService.markPaid(
                id,
                paymentDate,
                paymentMethod,
                referenceNumber
        );
    }

    @PatchMapping("/{id}/cancel")
    public PayrollResponse cancel(
            @PathVariable UUID id
    ) {
        return payrollService.cancel(id);
    }
}