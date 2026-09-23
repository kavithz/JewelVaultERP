package com.jewelvaulterp.accounting.service;

import com.jewelvaulterp.accounting.dto.CreateJournalEntryRequest;
import com.jewelvaulterp.accounting.dto.JournalEntryLineResponse;
import com.jewelvaulterp.accounting.dto.JournalEntryResponse;
import com.jewelvaulterp.accounting.entity.Account;
import com.jewelvaulterp.accounting.entity.JournalEntry;
import com.jewelvaulterp.accounting.entity.JournalEntryLine;
import com.jewelvaulterp.accounting.entity.JournalEntryStatus;
import com.jewelvaulterp.accounting.repository.AccountRepository;
import com.jewelvaulterp.accounting.repository.JournalEntryLineRepository;
import com.jewelvaulterp.accounting.repository.JournalEntryRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;

    public JournalEntryService(
            JournalEntryRepository journalEntryRepository,
            JournalEntryLineRepository journalEntryLineRepository,
            AccountRepository accountRepository,
            CompanyRepository companyRepository
    ) {
        this.journalEntryRepository = journalEntryRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.accountRepository = accountRepository;
        this.companyRepository = companyRepository;
    }

    public List<JournalEntryResponse> getAll() {
        return journalEntryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public JournalEntryResponse getById(UUID id) {
        return toResponse(findEntry(id));
    }

    public JournalEntryResponse create(CreateJournalEntryRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found")
                );

        journalEntryRepository
                .findByCompanyIdAndEntryNumber(
                        request.companyId(),
                        request.entryNumber()
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Journal entry number already exists for company"
                    );
                });

        validateLines(request);

        LocalDateTime now = LocalDateTime.now();

        JournalEntry entry = new JournalEntry(
                UUID.randomUUID(),
                company,
                request.entryNumber(),
                now,
                request.description(),
                request.referenceNumber(),
                JournalEntryStatus.DRAFT,
                now,
                now
        );

        journalEntryRepository.save(entry);

        for (var requestLine : request.lines()) {

            Account account = accountRepository
                    .findById(requestLine.accountId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Account not found: "
                                            + requestLine.accountId()
                            )
                    );

            if (!account.getCompany().getId().equals(request.companyId())) {
                throw new IllegalArgumentException(
                        "Account does not belong to the journal entry company"
                );
            }

            JournalEntryLine line = new JournalEntryLine(
                    UUID.randomUUID(),
                    entry,
                    account,
                    requestLine.description(),
                    requestLine.debitAmount(),
                    requestLine.creditAmount()
            );

            journalEntryLineRepository.save(line);
        }

        return toResponse(entry);
    }

    public JournalEntryResponse post(UUID id) {

        JournalEntry entry = findEntry(id);

        if (entry.getStatus() == JournalEntryStatus.POSTED) {
            throw new IllegalArgumentException(
                    "Journal entry is already posted"
            );
        }

        if (entry.getStatus() == JournalEntryStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cancelled journal entry cannot be posted"
            );
        }

        List<JournalEntryLine> lines =
                journalEntryLineRepository.findByJournalEntryId(id);

        if (lines.isEmpty()) {
            throw new IllegalArgumentException(
                    "Journal entry must contain at least one line"
            );
        }

        validateBalanced(lines);

        entry.setStatus(JournalEntryStatus.POSTED);

        return toResponse(journalEntryRepository.save(entry));
    }

    public JournalEntryResponse cancel(UUID id) {

        JournalEntry entry = findEntry(id);

        if (entry.getStatus() == JournalEntryStatus.POSTED) {
            throw new IllegalArgumentException(
                    "Posted journal entry cannot be cancelled"
            );
        }

        if (entry.getStatus() == JournalEntryStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Journal entry is already cancelled"
            );
        }

        entry.setStatus(JournalEntryStatus.CANCELLED);

        return toResponse(journalEntryRepository.save(entry));
    }

    private void validateLines(CreateJournalEntryRequest request) {

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (var line : request.lines()) {

            boolean hasDebit = line.debitAmount()
                    .compareTo(BigDecimal.ZERO) > 0;

            boolean hasCredit = line.creditAmount()
                    .compareTo(BigDecimal.ZERO) > 0;

            if (hasDebit == hasCredit) {
                throw new IllegalArgumentException(
                        "Each journal entry line must contain either debit or credit"
                );
            }

            totalDebit = totalDebit.add(line.debitAmount());
            totalCredit = totalCredit.add(line.creditAmount());
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException(
                    "Journal entry debits and credits must be balanced"
            );
        }
    }

    private void validateBalanced(List<JournalEntryLine> lines) {

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (JournalEntryLine line : lines) {
            totalDebit = totalDebit.add(line.getDebitAmount());
            totalCredit = totalCredit.add(line.getCreditAmount());
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalArgumentException(
                    "Journal entry debits and credits must be balanced"
            );
        }
    }

    private JournalEntry findEntry(UUID id) {
        return journalEntryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Journal entry not found"
                        )
                );
    }

    private JournalEntryResponse toResponse(JournalEntry entry) {

        List<JournalEntryLine> lines =
                journalEntryLineRepository.findByJournalEntryId(
                        entry.getId()
                );

        BigDecimal totalDebit = lines.stream()
                .map(JournalEntryLine::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = lines.stream()
                .map(JournalEntryLine::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<JournalEntryLineResponse> lineResponses =
                lines.stream()
                        .map(line -> new JournalEntryLineResponse(
                                line.getId(),
                                line.getAccount().getId(),
                                line.getDescription(),
                                line.getDebitAmount(),
                                line.getCreditAmount()
                        ))
                        .toList();

        return new JournalEntryResponse(
                entry.getId(),
                entry.getCompany().getId(),
                entry.getEntryNumber(),
                entry.getEntryDate(),
                entry.getDescription(),
                entry.getReferenceNumber(),
                entry.getStatus(),
                totalDebit,
                totalCredit,
                lineResponses,
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}