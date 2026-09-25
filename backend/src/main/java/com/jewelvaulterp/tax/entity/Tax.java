package com.jewelvaulterp.tax.entity;

import com.jewelvaulterp.company.entity.Company;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "taxes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_taxes_company_code",
                        columnNames = {"company_id", "code"}
                )
        }
)
public class Tax {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 30)
    private TaxType taxType;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal rate;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Tax() {
    }

    public Tax(
            Company company,
            String name,
            String code,
            TaxType taxType,
            BigDecimal rate,
            String description
    ) {
        this.company = company;
        this.name = name;
        this.code = code;
        this.taxType = taxType;
        this.rate = rate;
        this.description = description;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getDescription() {
        return description;
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
            String name,
            TaxType taxType,
            BigDecimal rate,
            String description
    ) {
        this.name = name;
        this.taxType = taxType;
        this.rate = rate;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
}