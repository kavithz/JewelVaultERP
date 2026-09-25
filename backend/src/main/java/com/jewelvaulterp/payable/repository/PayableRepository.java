package com.jewelvaulterp.payable.repository;

import com.jewelvaulterp.payable.entity.Payable;
import com.jewelvaulterp.payable.entity.PayableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayableRepository extends JpaRepository<Payable, UUID> {

    List<Payable> findByCompanyId(UUID companyId);

    List<Payable> findBySupplierId(UUID supplierId);

    List<Payable> findByStatus(PayableStatus status);
}