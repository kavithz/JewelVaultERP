package com.jewelvaulterp.expense.entity;

import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.company.entity.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDateTime expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private ExpensePaymentMethod paymentMethod;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Expense() {
    }

    public Expense(
            Company company,
            Branch branch,
            ExpenseCategory category,
            String description,
            BigDecimal amount,
            LocalDateTime expenseDate,
            ExpensePaymentMethod paymentMethod,
            String referenceNumber
    ) {
        this.company = company;
        this.branch = branch;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.status = ExpenseStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Company getCompany() { return company; }
    public Branch getBranch() { return branch; }
    public ExpenseCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getExpenseDate() { return expenseDate; }
    public ExpensePaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getReferenceNumber() { return referenceNumber; }
    public ExpenseStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void approve() {
        if (status == ExpenseStatus.CANCELLED) {
            throw new IllegalStateException("Cancelled expense cannot be approved");
        }

        status = ExpenseStatus.APPROVED;
        updatedAt = LocalDateTime.now();
    }

    public void markPaid() {
        if (status != ExpenseStatus.APPROVED) {
            throw new IllegalStateException("Only approved expenses can be marked as paid");
        }

        status = ExpenseStatus.PAID;
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == ExpenseStatus.PAID) {
            throw new IllegalStateException("Paid expense cannot be cancelled");
        }

        status = ExpenseStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }
}
