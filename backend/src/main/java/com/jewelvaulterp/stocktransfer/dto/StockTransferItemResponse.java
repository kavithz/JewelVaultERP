package com.jewelvaulterp.stocktransfer.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record StockTransferItemResponse(
        UUID id,
        UUID productId,
        BigDecimal quantity
) {}