package com.jewelvaulterp.customer.repository;

import com.jewelvaulterp.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByCompanyIdAndCode(
            UUID companyId,
            String code
    );

    long countByActiveTrue();

    long countByActiveFalse();
}