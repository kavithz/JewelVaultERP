package com.jewelvaulterp.manufacturing.entity;

import com.jewelvaulterp.product.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "manufacturing_order_items")
public class ManufacturingOrderItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "manufacturing_order_id", nullable = false)
    private ManufacturingOrder manufacturingOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 30)
    private ManufacturingItemType itemType;

    protected ManufacturingOrderItem() {
    }

    public ManufacturingOrderItem(
            UUID id,
            ManufacturingOrder manufacturingOrder,
            Product product,
            BigDecimal quantity,
            ManufacturingItemType itemType
    ) {
        this.id = id;
        this.manufacturingOrder = manufacturingOrder;
        this.product = product;
        this.quantity = quantity;
        this.itemType = itemType;
    }

    public UUID getId() {
        return id;
    }

    public ManufacturingOrder getManufacturingOrder() {
        return manufacturingOrder;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public ManufacturingItemType getItemType() {
        return itemType;
    }
}