package com.jewelvaulterp.payable.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.invoice.entity.Invoice;
import com.jewelvaulterp.supplier.entity.Supplier;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payables")
public class Payable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "outstanding_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal outstandingAmount;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayableStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Payable() {
    }

    public Payable(
            Company company,
            Supplier supplier,
            Invoice invoice,
            BigDecimal amount,
            LocalDateTime dueDate
    ) {
        this.company = company;
        this.supplier = supplier;
        this.invoice = invoice;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.outstandingAmount = amount;
        this.dueDate = dueDate;
        this.status = PayableStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void recordPayment(BigDecimal paymentAmount) {

        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (paymentAmount.compareTo(outstandingAmount) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount cannot exceed outstanding amount"
            );
        }

        this.paidAmount = this.paidAmount.add(paymentAmount);
        this.outstandingAmount = this.amount.subtract(this.paidAmount);

        if (this.outstandingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = PayableStatus.PAID;
        } else {
            this.status = PayableStatus.PARTIALLY_PAID;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void markOverdue() {

        if (this.outstandingAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = PayableStatus.OVERDUE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        this.status = PayableStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public PayableStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}