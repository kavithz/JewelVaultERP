package com.jewelvaulterp.accounting.dto;

import com.jewelvaulterp.accounting.entity.JournalEntryStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id,
        UUID companyId,
        String entryNumber,
        LocalDateTime entryDate,
        String description,
        String referenceNumber,
        JournalEntryStatus status,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        List<JournalEntryLineResponse> lines,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}