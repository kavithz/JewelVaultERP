package com.jewelvaulterp.stockadjustment.entity;

import com.jewelvaulterp.product.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_adjustment_items")
public class StockAdjustmentItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adjustment_id", nullable = false)
    private StockAdjustment adjustment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "adjustment_type",
            nullable = false,
            length = 30
    )
    private AdjustmentType adjustmentType;

    protected StockAdjustmentItem() {
    }

    public StockAdjustmentItem(
            UUID id,
            StockAdjustment adjustment,
            Product product,
            BigDecimal quantity,
            AdjustmentType adjustmentType
    ) {
        this.id = id;
        this.adjustment = adjustment;
        this.product = product;
        this.quantity = quantity;
        this.adjustmentType = adjustmentType;
    }

    public UUID getId() {
        return id;
    }

    public StockAdjustment getAdjustment() {
        return adjustment;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public AdjustmentType getAdjustmentType() {
        return adjustmentType;
    }
}