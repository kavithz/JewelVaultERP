package com.jewelvaulterp.accounting.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "journal_entry_lines")
public class JournalEntryLine {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(length = 500)
    private String description;

    @Column(name = "debit_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal creditAmount;

    protected JournalEntryLine() {
    }

    public JournalEntryLine(
            UUID id,
            JournalEntry journalEntry,
            Account account,
            String description,
            BigDecimal debitAmount,
            BigDecimal creditAmount
    ) {
        this.id = id;
        this.journalEntry = journalEntry;
        this.account = account;
        this.description = description;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
    }

    public UUID getId() {
        return id;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public Account getAccount() {
        return account;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }
}