package com.jewelvaulterp.stocktransfer.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "stock_transfers",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stock_transfers_company_number",
                columnNames = {"company_id", "transfer_number"}
        )
)
public class StockTransfer {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_warehouse_id", nullable = false)
    private Warehouse sourceWarehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_warehouse_id", nullable = false)
    private Warehouse destinationWarehouse;

    @Column(name = "transfer_number", nullable = false, length = 100)
    private String transferNumber;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockTransferStatus status;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected StockTransfer() {
    }

    public StockTransfer(
            UUID id,
            Company company,
            Warehouse sourceWarehouse,
            Warehouse destinationWarehouse,
            String transferNumber,
            LocalDateTime transferDate,
            StockTransferStatus status,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.sourceWarehouse = sourceWarehouse;
        this.destinationWarehouse = destinationWarehouse;
        this.transferNumber = transferNumber;
        this.transferDate = transferDate;
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

    public Warehouse getSourceWarehouse() {
        return sourceWarehouse;
    }

    public Warehouse getDestinationWarehouse() {
        return destinationWarehouse;
    }

    public String getTransferNumber() {
        return transferNumber;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public StockTransferStatus getStatus() {
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

    public void setStatus(StockTransferStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}