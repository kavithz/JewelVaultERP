package com.jewelvaulterp.invoice.repository;

import com.jewelvaulterp.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByCompanyIdAndInvoiceNumber(
            UUID companyId,
            String invoiceNumber
    );
}