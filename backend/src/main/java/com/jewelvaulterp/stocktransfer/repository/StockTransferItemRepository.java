package com.jewelvaulterp.stocktransfer.repository;

import com.jewelvaulterp.stocktransfer.entity.StockTransferItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockTransferItemRepository
        extends JpaRepository<StockTransferItem, UUID> {

    List<StockTransferItem> findByTransferId(UUID transferId);
}