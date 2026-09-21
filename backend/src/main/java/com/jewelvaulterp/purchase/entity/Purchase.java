package com.jewelvaulterp.purchase.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "purchases",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_purchases_company_number",
                columnNames = {"company_id", "purchase_number"}
        )
)
public class Purchase {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "purchase_number", nullable = false, length = 100)
    private String purchaseNumber;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PurchaseStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Purchase() {
    }

    public Purchase(
            UUID id,
            Company company,
            Supplier supplier,
            Warehouse warehouse,
            String purchaseNumber,
            LocalDateTime purchaseDate,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            PurchaseStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.supplier = supplier;
        this.warehouse = warehouse;
        this.purchaseNumber = purchaseNumber;
        this.purchaseDate = purchaseDate;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
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

    public Supplier getSupplier() {
        return supplier;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(PurchaseStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}