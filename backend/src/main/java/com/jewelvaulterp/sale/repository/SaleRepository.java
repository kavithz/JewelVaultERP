package com.jewelvaulterp.sale.repository;

import com.jewelvaulterp.sale.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    Page<Sale> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Sale> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    List<Sale> findByCompanyId(UUID companyId);

    Optional<Sale> findByCompanyIdAndSaleNumber(
            UUID companyId,
            String saleNumber
    );
}