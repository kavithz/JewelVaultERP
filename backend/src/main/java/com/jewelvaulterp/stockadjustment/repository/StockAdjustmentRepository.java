package com.jewelvaulterp.stockadjustment.repository;

import com.jewelvaulterp.stockadjustment.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockAdjustmentRepository
        extends JpaRepository<StockAdjustment, UUID> {

    Optional<StockAdjustment> findByCompanyIdAndAdjustmentNumber(
            UUID companyId,
            String adjustmentNumber
    );
}