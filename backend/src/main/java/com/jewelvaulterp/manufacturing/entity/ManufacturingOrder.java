package com.jewelvaulterp.manufacturing.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "manufacturing_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_manufacturing_orders_company_number",
                columnNames = {"company_id", "order_number"}
        )
)
public class ManufacturingOrder {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(
            name = "order_number",
            nullable = false,
            length = 100
    )
    private String orderNumber;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ManufacturingOrderStatus status;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ManufacturingOrder() {
    }

    public ManufacturingOrder(
            UUID id,
            Company company,
            Warehouse warehouse,
            String orderNumber,
            LocalDateTime orderDate,
            ManufacturingOrderStatus status,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.warehouse = warehouse;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public ManufacturingOrderStatus getStatus() {
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

    public void setStatus(ManufacturingOrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}