package com.jewelvaulterp.accounting.dto;

import com.jewelvaulterp.accounting.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @NotBlank @Size(max = 50) String accountCode,
        @NotBlank @Size(max = 150) String name,
        @NotNull AccountType accountType
) {}