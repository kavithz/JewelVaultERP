package com.jewelvaulterp.tax.repository;

import com.jewelvaulterp.tax.entity.Tax;
import com.jewelvaulterp.tax.entity.TaxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TaxRepository extends JpaRepository<Tax, UUID> {

    Page<Tax> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Tax> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    List<Tax> findByCompanyId(UUID companyId);

    List<Tax> findByCompanyIdAndActiveTrue(UUID companyId);

    List<Tax> findByCompanyIdAndTaxType(UUID companyId, TaxType taxType);
}