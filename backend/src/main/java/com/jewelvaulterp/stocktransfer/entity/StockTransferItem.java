package com.jewelvaulterp.stocktransfer.entity;

import com.jewelvaulterp.product.entity.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_transfer_items")
public class StockTransferItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", nullable = false)
    private StockTransfer transfer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    protected StockTransferItem() {
    }

    public StockTransferItem(
            UUID id,
            StockTransfer transfer,
            Product product,
            BigDecimal quantity
    ) {
        this.id = id;
        this.transfer = transfer;
        this.product = product;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public StockTransfer getTransfer() {
        return transfer;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}