package com.jewelvaulterp.payment.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.supplier.entity.Supplier;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payments_company_number",
                columnNames = {"company_id", "payment_number"}
        )
)
public class Payment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(
            name = "payment_number",
            nullable = false,
            length = 100
    )
    private String paymentNumber;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Payment() {
    }

    public Payment(
            UUID id,
            Company company,
            String paymentNumber,
            LocalDateTime paymentDate,
            PaymentType paymentType,
            PaymentMethod paymentMethod,
            BigDecimal amount,
            String referenceNumber,
            String notes,
            PaymentStatus status,
            Customer customer,
            Supplier supplier,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.paymentNumber = paymentNumber;
        this.paymentDate = paymentDate;
        this.paymentType = paymentType;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.status = status;
        this.customer = customer;
        this.supplier = supplier;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}