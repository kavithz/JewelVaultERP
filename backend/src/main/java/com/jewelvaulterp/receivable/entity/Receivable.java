package com.jewelvaulterp.receivable.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.invoice.entity.Invoice;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "receivables")
public class Receivable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

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
    private ReceivableStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Receivable() {
    }

    public Receivable(
            Company company,
            Customer customer,
            Invoice invoice,
            BigDecimal amount,
            LocalDateTime dueDate
    ) {
        this.company = company;
        this.customer = customer;
        this.invoice = invoice;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.outstandingAmount = amount;
        this.dueDate = dueDate;
        this.status = ReceivableStatus.OPEN;
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
            this.status = ReceivableStatus.PAID;
        } else {
            this.status = ReceivableStatus.PARTIALLY_PAID;
        }

        this.updatedAt = LocalDateTime.now();
    }

    public void markOverdue() {

        if (this.outstandingAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = ReceivableStatus.OVERDUE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        this.status = ReceivableStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public Customer getCustomer() {
        return customer;
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

    public ReceivableStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}