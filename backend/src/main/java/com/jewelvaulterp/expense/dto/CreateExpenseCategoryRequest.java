package com.jewelvaulterp.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateExpenseCategoryRequest(
        @NotNull UUID companyId,
        @NotBlank String name,
        String description
) {
}
