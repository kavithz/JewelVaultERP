package com.jewelvaulterp.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeRequest(
        @NotNull UUID companyId,
        UUID branchId,
        @NotBlank String employeeNumber,
        @NotBlank String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String department,
        String designation,
        @NotNull LocalDate joiningDate
) {
}