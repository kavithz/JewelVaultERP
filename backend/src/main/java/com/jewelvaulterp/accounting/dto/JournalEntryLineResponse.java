package com.jewelvaulterp.accounting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record JournalEntryLineResponse(
        UUID id,
        UUID accountId,
        String description,
        BigDecimal debitAmount,
        BigDecimal creditAmount
) {}