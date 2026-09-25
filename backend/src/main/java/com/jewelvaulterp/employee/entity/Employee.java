package com.jewelvaulterp.employee.entity;

import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.company.entity.Company;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employees_company_number",
                        columnNames = {"company_id", "employee_number"}
                )
        }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "employee_number", nullable = false, length = 50)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 150)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Employee() {
    }

    public Employee(
            Company company,
            Branch branch,
            String employeeNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address,
            String department,
            String designation,
            LocalDate joiningDate
    ) {
        this.company = company;
        this.branch = branch;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.department = department;
        this.designation = designation;
        this.employmentStatus = EmploymentStatus.ACTIVE;
        this.joiningDate = joiningDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Company getCompany() { return company; }
    public Branch getBranch() { return branch; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getDepartment() { return department; }
    public String getDesignation() { return designation; }
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public LocalDate getExitDate() { return exitDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updateStatus(EmploymentStatus status) {
        this.employmentStatus = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setExitDate(LocalDate exitDate) {
        this.exitDate = exitDate;
        this.updatedAt = LocalDateTime.now();
    }
}