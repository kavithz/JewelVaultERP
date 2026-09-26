package com.jewelvaulterp.branch.repository;

import com.jewelvaulterp.branch.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Page<Branch> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Branch> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    Optional<Branch> findByCompanyIdAndCode(UUID companyId, String code);

    long countByActiveTrue();

    long countByActiveFalse();
}