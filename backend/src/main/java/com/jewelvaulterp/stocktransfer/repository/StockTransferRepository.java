package com.jewelvaulterp.stocktransfer.repository;

import com.jewelvaulterp.stocktransfer.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockTransferRepository
        extends JpaRepository<StockTransfer, UUID> {

    Optional<StockTransfer> findByCompanyIdAndTransferNumber(
            UUID companyId,
            String transferNumber
    );
}