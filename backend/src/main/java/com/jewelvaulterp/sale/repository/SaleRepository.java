package com.jewelvaulterp.sale.repository;

import com.jewelvaulterp.sale.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    Optional<Sale> findByCompanyIdAndSaleNumber(
            UUID companyId,
            String saleNumber
    );
}