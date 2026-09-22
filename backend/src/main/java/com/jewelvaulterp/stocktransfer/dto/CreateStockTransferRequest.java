package com.jewelvaulterp.stocktransfer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateStockTransferRequest(
        @NotNull UUID companyId,
        @NotNull UUID sourceWarehouseId,
        @NotNull UUID destinationWarehouseId,
        @NotBlank @Size(max = 100) String transferNumber,
        LocalDateTime transferDate,
        @Size(max = 500) String notes,
        @NotEmpty List<@Valid CreateStockTransferItemRequest> items
) {}