package com.jewelvaulterp.accounting.dto;

import com.jewelvaulterp.accounting.entity.AccountType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID companyId,
        String accountCode,
        String name,
        AccountType accountType,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}