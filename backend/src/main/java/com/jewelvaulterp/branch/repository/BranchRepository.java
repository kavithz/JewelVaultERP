package com.jewelvaulterp.branch.repository;

import com.jewelvaulterp.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByCompanyIdAndCode(UUID companyId, String code);

    long countByActiveTrue();

    long countByActiveFalse();
}