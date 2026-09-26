package com.jewelvaulterp.integration.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationPayrollResponse(
        UUID id,
        UUID companyId,
        UUID employeeId,
        Integer payrollYear,
        Integer payrollMonth,
        BigDecimal grossSalary,
        BigDecimal deductions,
        BigDecimal netSalary,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
