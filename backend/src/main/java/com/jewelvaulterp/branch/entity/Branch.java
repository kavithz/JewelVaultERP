package com.jewelvaulterp.branch.entity;

import com.jewelvaulterp.company.entity.Company;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "branches")
public class Branch {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Branch() {
    }

    public Branch(
            UUID id,
            Company company,
            String name,
            String code,
            String address,
            String city,
            String countryCode,
            String phone,
            String email,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.company = company;
        this.name = name;
        this.code = code;
        this.address = address;
        this.city = city;
        this.countryCode = countryCode;
        this.phone = phone;
        this.email = email;
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

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
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
            String code,
            String address,
            String city,
            String countryCode,
            String phone,
            String email
    ) {
        this.name = name;
        this.code = code;
        this.address = address;
        this.city = city;
        this.countryCode = countryCode;
        this.phone = phone;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }
}