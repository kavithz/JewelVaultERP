package com.jewelvaulterp.sale.repository;

import com.jewelvaulterp.sale.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findByCompanyId(UUID companyId);

    Optional<Sale> findByCompanyIdAndSaleNumber(
            UUID companyId,
            String saleNumber
    );
}