package com.jewelvaulterp.accounting.repository;

import com.jewelvaulterp.accounting.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JournalEntryLineRepository
        extends JpaRepository<JournalEntryLine, UUID> {

    List<JournalEntryLine> findByJournalEntryId(UUID journalEntryId);

    List<JournalEntryLine> findByAccountId(UUID accountId);
}