package com.jewelvaulterp.accounting.repository;

import com.jewelvaulterp.accounting.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    Optional<Account> findByCompanyIdAndAccountCode(
            UUID companyId,
            String accountCode
    );
}