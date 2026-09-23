package com.jewelvaulterp.accounting.repository;

import com.jewelvaulterp.accounting.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository
        extends JpaRepository<JournalEntry, UUID> {

    Optional<JournalEntry> findByCompanyIdAndEntryNumber(
            UUID companyId,
            String entryNumber
    );
}