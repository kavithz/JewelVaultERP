package com.jewelvaulterp.stockadjustment.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "stock_adjustments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stock_adjustments_company_number",
                columnNames = {"company_id", "adjustment_number"}
        )
)
public class StockAdjustment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(
            name = "adjustment_number",
            nullable = false,
            length = 100
    )
    private String adjustmentNumber;

    @Column(name = "adjustment_date", nullable = false)
    private LocalDateTime adjustmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockAdjustmentStatus status;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected StockAdjustment() {
    }

    public StockAdjustment(
            UUID id,
            Company company,
            Warehouse warehouse,
            String adjustmentNumber,
            LocalDateTime adjustmentDate,
            StockAdjustmentStatus status,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.warehouse = warehouse;
        this.adjustmentNumber = adjustmentNumber;
        this.adjustmentDate = adjustmentDate;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public String getAdjustmentNumber() {
        return adjustmentNumber;
    }

    public LocalDateTime getAdjustmentDate() {
        return adjustmentDate;
    }

    public StockAdjustmentStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(StockAdjustmentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}