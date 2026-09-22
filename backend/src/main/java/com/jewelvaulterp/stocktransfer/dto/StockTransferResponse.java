package com.jewelvaulterp.stocktransfer.dto;

import com.jewelvaulterp.stocktransfer.entity.StockTransferStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record StockTransferResponse(
        UUID id,
        UUID companyId,
        UUID sourceWarehouseId,
        UUID destinationWarehouseId,
        String transferNumber,
        LocalDateTime transferDate,
        StockTransferStatus status,
        String notes,
        List<StockTransferItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}