package com.jewelvaulterp.accounting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateJournalEntryRequest(
        @NotNull UUID companyId,
        @NotBlank @Size(max = 100) String entryNumber,
        @Size(max = 500) String description,
        @Size(max = 100) String referenceNumber,
        @NotEmpty List<@Valid CreateJournalEntryLineRequest> lines
) {}