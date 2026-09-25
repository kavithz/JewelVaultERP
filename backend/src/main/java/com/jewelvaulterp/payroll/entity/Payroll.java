package com.jewelvaulterp.payroll.entity;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payroll",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payroll_employee_period",
                        columnNames = {
                                "employee_id",
                                "payroll_year",
                                "payroll_month"
                        }
                )
        }
)
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(name = "payroll_year", nullable = false)
    private Integer payrollYear;

    @Column(name = "payroll_month", nullable = false)
    private Integer payrollMonth;

    @Column(name = "basic_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "allowances", nullable = false, precision = 14, scale = 2)
    private BigDecimal allowances;

    @Column(name = "deductions", nullable = false, precision = 14, scale = 2)
    private BigDecimal deductions;

    @Column(name = "gross_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "net_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PayrollPaymentMethod paymentMethod;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayrollStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Payroll() {
    }

    public Payroll(
            Company company,
            User employee,
            Integer payrollYear,
            Integer payrollMonth,
            BigDecimal basicSalary,
            BigDecimal allowances,
            BigDecimal deductions
    ) {
        this.company = company;
        this.employee = employee;
        this.payrollYear = payrollYear;
        this.payrollMonth = payrollMonth;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.deductions = deductions;
        this.grossSalary = basicSalary.add(allowances);
        this.netSalary = this.grossSalary.subtract(deductions);
        this.status = PayrollStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public User getEmployee() {
        return employee;
    }

    public Integer getPayrollYear() {
        return payrollYear;
    }

    public Integer getPayrollMonth() {
        return payrollMonth;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public PayrollPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public PayrollStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void process() {
        this.status = PayrollStatus.PROCESSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markPaid(
            LocalDate paymentDate,
            PayrollPaymentMethod paymentMethod,
            String referenceNumber
    ) {
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.status = PayrollStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = PayrollStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}