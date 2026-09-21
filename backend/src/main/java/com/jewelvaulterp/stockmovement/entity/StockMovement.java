package com.jewelvaulterp.stockmovement.entity;

import com.jewelvaulterp.inventory.entity.Inventory;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private MovementType movementType;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(length = 500)
    private String notes;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected StockMovement() {
    }

    public StockMovement(
            UUID id,
            Inventory inventory,
            MovementType movementType,
            BigDecimal quantity,
            String referenceNumber,
            String notes,
            LocalDateTime movementDate,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.inventory = inventory;
        this.movementType = movementType;
        this.quantity = quantity;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.movementDate = movementDate;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getMovementDate() {
        return movementDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}