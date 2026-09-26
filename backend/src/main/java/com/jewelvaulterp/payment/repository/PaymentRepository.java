package com.jewelvaulterp.payment.repository;

import com.jewelvaulterp.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

    Page<Payment> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Payment> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    List<Payment> findByCompanyId(UUID companyId);

    Optional<Payment> findByCompanyIdAndPaymentNumber(
            UUID companyId,
            String paymentNumber
    );
}