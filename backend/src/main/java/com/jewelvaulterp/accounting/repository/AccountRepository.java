package com.jewelvaulterp.accounting.repository;

import com.jewelvaulterp.accounting.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    List<Account> findByCompanyId(UUID companyId);

    Optional<Account> findByCompanyIdAndAccountCode(
            UUID companyId,
            String accountCode
    );
}