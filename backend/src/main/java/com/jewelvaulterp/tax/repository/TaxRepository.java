package com.jewelvaulterp.tax.repository;

import com.jewelvaulterp.tax.entity.Tax;
import com.jewelvaulterp.tax.entity.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaxRepository extends JpaRepository<Tax, UUID> {

    List<Tax> findByCompanyId(UUID companyId);

    List<Tax> findByCompanyIdAndActiveTrue(UUID companyId);

    List<Tax> findByCompanyIdAndTaxType(UUID companyId, TaxType taxType);
}