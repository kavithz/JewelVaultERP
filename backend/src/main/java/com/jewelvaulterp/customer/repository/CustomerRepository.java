package com.jewelvaulterp.customer.repository;

import com.jewelvaulterp.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Customer> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    Optional<Customer> findByCompanyIdAndCode(
            UUID companyId,
            String code
    );

    long countByActiveTrue();

    long countByActiveFalse();
}