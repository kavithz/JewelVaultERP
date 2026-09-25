package com.jewelvaulterp.accounting.repository;

import com.jewelvaulterp.accounting.entity.JournalEntryLine;
import com.jewelvaulterp.accounting.entity.JournalEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JournalEntryLineRepository
        extends JpaRepository<JournalEntryLine, UUID> {

    List<JournalEntryLine> findByJournalEntryId(UUID journalEntryId);

    List<JournalEntryLine> findByAccountId(UUID accountId);

    @Query("""
            SELECT l
            FROM JournalEntryLine l
            JOIN FETCH l.journalEntry j
            JOIN FETCH l.account a
            WHERE j.company.id = :companyId
              AND j.status = :status
            ORDER BY a.accountCode
            """)
    List<JournalEntryLine> findByCompanyIdAndJournalStatus(
            @Param("companyId") UUID companyId,
            @Param("status") JournalEntryStatus status
    );
}