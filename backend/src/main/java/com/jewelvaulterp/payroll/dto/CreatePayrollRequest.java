package com.jewelvaulterp.payroll.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePayrollRequest(
        @NotNull UUID companyId,

        @NotNull UUID employeeId,

        @NotNull
        @Min(2000)
        Integer payrollYear,

        @NotNull
        @Min(1)
        @Max(12)
        Integer payrollMonth,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal basicSalary,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal allowances,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal deductions
) {
}