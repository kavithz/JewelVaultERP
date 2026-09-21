package com.jewelvaulterp.product.entity;

import com.jewelvaulterp.company.entity.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "jewellery_type", nullable = false, length = 100)
    private String jewelleryType;

    @Column(name = "metal_type", nullable = false, length = 100)
    private String metalType;

    @Column(nullable = false, length = 50)
    private String purity;

    @Column(name = "gross_weight", nullable = false, precision = 12, scale = 3)
    private BigDecimal grossWeight;

    @Column(name = "net_weight", nullable = false, precision = 12, scale = 3)
    private BigDecimal netWeight;

    @Column(name = "making_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal makingCharge;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Product() {
    }

    public Product(
            UUID id,
            Company company,
            String sku,
            String name,
            String jewelleryType,
            String metalType,
            String purity,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal makingCharge,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.sku = sku;
        this.name = name;
        this.jewelleryType = jewelleryType;
        this.metalType = metalType;
        this.purity = purity;
        this.grossWeight = grossWeight;
        this.netWeight = netWeight;
        this.makingCharge = makingCharge;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getJewelleryType() {
        return jewelleryType;
    }

    public String getMetalType() {
        return metalType;
    }

    public String getPurity() {
        return purity;
    }

    public BigDecimal getGrossWeight() {
        return grossWeight;
    }

    public BigDecimal getNetWeight() {
        return netWeight;
    }

    public BigDecimal getMakingCharge() {
        return makingCharge;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void update(
            String sku,
            String name,
            String jewelleryType,
            String metalType,
            String purity,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal makingCharge
    ) {
        this.sku = sku;
        this.name = name;
        this.jewelleryType = jewelleryType;
        this.metalType = metalType;
        this.purity = purity;
        this.grossWeight = grossWeight;
        this.netWeight = netWeight;
        this.makingCharge = makingCharge;
        this.updatedAt = LocalDateTime.now();
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }
}