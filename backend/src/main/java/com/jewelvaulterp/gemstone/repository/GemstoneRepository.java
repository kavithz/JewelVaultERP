package com.jewelvaulterp.gemstone.repository;

import com.jewelvaulterp.gemstone.entity.Gemstone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GemstoneRepository extends JpaRepository<Gemstone, UUID> {

    Optional<Gemstone> findByCompanyIdAndCode(UUID companyId, String code);

    long countByActiveTrue();

    long countByActiveFalse();
}