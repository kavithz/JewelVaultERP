package com.jewelvaulterp.integration.dto;

import com.jewelvaulterp.employee.entity.EmploymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationEmployeeResponse(
        UUID id,
        UUID companyId,
        UUID branchId,
        String employeeNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String department,
        String designation,
        EmploymentStatus employmentStatus,
        LocalDate joiningDate,
        LocalDate exitDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
