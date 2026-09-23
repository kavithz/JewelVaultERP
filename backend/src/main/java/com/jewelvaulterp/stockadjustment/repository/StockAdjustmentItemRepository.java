package com.jewelvaulterp.stockadjustment.repository;

import com.jewelvaulterp.stockadjustment.entity.StockAdjustmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockAdjustmentItemRepository
        extends JpaRepository<StockAdjustmentItem, UUID> {

    List<StockAdjustmentItem> findByAdjustmentId(UUID adjustmentId);
}