package com.jewelvaulterp.invoice.repository;

import com.jewelvaulterp.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Invoice> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    Optional<Invoice> findByCompanyIdAndInvoiceNumber(
            UUID companyId,
            String invoiceNumber
    );
}