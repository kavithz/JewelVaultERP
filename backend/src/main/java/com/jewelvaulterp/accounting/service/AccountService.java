package com.jewelvaulterp.accounting.service;

import com.jewelvaulterp.accounting.dto.AccountResponse;
import com.jewelvaulterp.accounting.dto.CreateAccountRequest;
import com.jewelvaulterp.accounting.dto.UpdateAccountRequest;
import com.jewelvaulterp.accounting.entity.Account;
import com.jewelvaulterp.accounting.repository.AccountRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;

    public AccountService(
            AccountRepository accountRepository,
            CompanyRepository companyRepository
    ) {
        this.accountRepository = accountRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAll() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(UUID id) {
        return toResponse(findAccount(id));
    }

    public AccountResponse create(CreateAccountRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found")
                );

        accountRepository
                .findByCompanyIdAndAccountCode(
                        request.companyId(),
                        request.accountCode()
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Account code already exists for company"
                    );
                });

        LocalDateTime now = LocalDateTime.now();

        Account account = new Account(
                UUID.randomUUID(),
                company,
                request.accountCode(),
                request.name(),
                request.accountType(),
                true,
                now,
                now
        );

        return toResponse(accountRepository.save(account));
    }

    public AccountResponse update(
            UUID id,
            UpdateAccountRequest request
    ) {
        Account account = findAccount(id);

        accountRepository
                .findByCompanyIdAndAccountCode(
                        account.getCompany().getId(),
                        request.accountCode()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Account code already exists for company"
                    );
                });

        account.update(
                request.accountCode(),
                request.name(),
                request.accountType()
        );

        return toResponse(accountRepository.save(account));
    }

    public AccountResponse setActive(
            UUID id,
            boolean active
    ) {
        Account account = findAccount(id);
        account.setActive(active);

        return toResponse(accountRepository.save(account));
    }

    private Account findAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Account not found")
                );
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCompany().getId(),
                account.getAccountCode(),
                account.getName(),
                account.getAccountType(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}