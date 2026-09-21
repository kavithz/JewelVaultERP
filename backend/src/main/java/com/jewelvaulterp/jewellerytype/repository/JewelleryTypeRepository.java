package com.jewelvaulterp.jewellerytype.repository;

import com.jewelvaulterp.jewellerytype.entity.JewelleryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JewelleryTypeRepository extends JpaRepository<JewelleryType, UUID> {

    Optional<JewelleryType> findByCompanyIdAndCode(UUID companyId, String code);

    long countByActiveTrue();

    long countByActiveFalse();
}