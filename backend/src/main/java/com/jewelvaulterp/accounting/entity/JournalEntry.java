package com.jewelvaulterp.accounting.entity;

import com.jewelvaulterp.company.entity.Company;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "journal_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_journal_entries_company_number",
                columnNames = {"company_id", "entry_number"}
        )
)
public class JournalEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "entry_number", nullable = false, length = 100)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Column(length = 500)
    private String description;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JournalEntryStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected JournalEntry() {
    }

    public JournalEntry(
            UUID id,
            Company company,
            String entryNumber,
            LocalDateTime entryDate,
            String description,
            String referenceNumber,
            JournalEntryStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.entryNumber = entryNumber;
        this.entryDate = entryDate;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public LocalDateTime getEntryDate() {
        return entryDate;
    }

    public String getDescription() {
        return description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public JournalEntryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(JournalEntryStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}