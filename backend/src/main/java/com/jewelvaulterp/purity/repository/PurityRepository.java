package com.jewelvaulterp.purity.repository;

import com.jewelvaulterp.purity.entity.Purity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurityRepository extends JpaRepository<Purity, UUID> {

    Optional<Purity> findByCompanyIdAndCode(UUID companyId, String code);

    long countByActiveTrue();

    long countByActiveFalse();
}