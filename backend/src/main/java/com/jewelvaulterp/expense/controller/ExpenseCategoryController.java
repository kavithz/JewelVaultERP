package com.jewelvaulterp.expense.controller;

import com.jewelvaulterp.expense.dto.CreateExpenseCategoryRequest;
import com.jewelvaulterp.expense.entity.ExpenseCategory;
import com.jewelvaulterp.expense.service.ExpenseCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expense-categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService service;

    public ExpenseCategoryController(ExpenseCategoryService service) {
        this.service = service;
    }

    @GetMapping("/company/{companyId}")
    public List<ExpenseCategory> getByCompany(@PathVariable UUID companyId) {
        return service.getByCompany(companyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseCategory create(
            @Valid @RequestBody CreateExpenseCategoryRequest request
    ) {
        return service.create(request);
    }
}
