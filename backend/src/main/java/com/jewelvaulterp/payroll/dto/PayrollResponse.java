package com.jewelvaulterp.payroll.dto;

import com.jewelvaulterp.payroll.entity.PayrollPaymentMethod;
import com.jewelvaulterp.payroll.entity.PayrollStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollResponse(
        UUID id,
        UUID companyId,
        UUID employeeId,
        Integer payrollYear,
        Integer payrollMonth,
        BigDecimal basicSalary,
        BigDecimal allowances,
        BigDecimal deductions,
        BigDecimal grossSalary,
        BigDecimal netSalary,
        LocalDate paymentDate,
        PayrollPaymentMethod paymentMethod,
        String referenceNumber,
        PayrollStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}