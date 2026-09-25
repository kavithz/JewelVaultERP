package com.jewelvaulterp.expense.controller;

import com.jewelvaulterp.expense.dto.CreateExpenseRequest;
import com.jewelvaulterp.expense.dto.ExpenseResponse;
import com.jewelvaulterp.expense.entity.ExpenseStatus;
import com.jewelvaulterp.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<ExpenseResponse> getAll() {
        return expenseService.getAll();
    }

    @GetMapping("/{id}")
    public ExpenseResponse getById(@PathVariable UUID id) {
        return expenseService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<ExpenseResponse> getByCompany(@PathVariable UUID companyId) {
        return expenseService.getByCompany(companyId);
    }

    @GetMapping("/category/{categoryId}")
    public List<ExpenseResponse> getByCategory(@PathVariable UUID categoryId) {
        return expenseService.getByCategory(categoryId);
    }

    @GetMapping("/status/{status}")
    public List<ExpenseResponse> getByStatus(@PathVariable ExpenseStatus status) {
        return expenseService.getByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        return expenseService.create(request);
    }

    @PatchMapping("/{id}/approve")
    public ExpenseResponse approve(@PathVariable UUID id) {
        return expenseService.approve(id);
    }

    @PatchMapping("/{id}/paid")
    public ExpenseResponse markPaid(@PathVariable UUID id) {
        return expenseService.markPaid(id);
    }

    @PatchMapping("/{id}/cancel")
    public ExpenseResponse cancel(@PathVariable UUID id) {
        return expenseService.cancel(id);
    }
}
