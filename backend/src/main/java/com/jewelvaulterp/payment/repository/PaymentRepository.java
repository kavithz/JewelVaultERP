package com.jewelvaulterp.payment.repository;

import com.jewelvaulterp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository
        extends JpaRepository<Payment, UUID> {

    List<Payment> findByCompanyId(UUID companyId);

    Optional<Payment> findByCompanyIdAndPaymentNumber(
            UUID companyId,
            String paymentNumber
    );
}