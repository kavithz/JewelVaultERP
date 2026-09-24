package com.jewelvaulterp.receivable.repository;

import com.jewelvaulterp.receivable.entity.Receivable;
import com.jewelvaulterp.receivable.entity.ReceivableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReceivableRepository extends JpaRepository<Receivable, UUID> {

    List<Receivable> findByCompanyId(UUID companyId);

    List<Receivable> findByCustomerId(UUID customerId);

    List<Receivable> findByStatus(ReceivableStatus status);
}