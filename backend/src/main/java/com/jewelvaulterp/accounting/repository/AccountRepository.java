package com.jewelvaulterp.accounting.repository;

import com.jewelvaulterp.accounting.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    Page<Account> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Account> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    List<Account> findByCompanyId(UUID companyId);

    Optional<Account> findByCompanyIdAndAccountCode(
            UUID companyId,
            String accountCode
    );
}