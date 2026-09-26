package com.jewelvaulterp.employee.repository;

import com.jewelvaulterp.employee.entity.Employee;
import com.jewelvaulterp.employee.entity.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface EmployeeRepository
        extends JpaRepository<Employee, UUID> {

    Page<Employee> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Employee> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    List<Employee> findByCompanyId(UUID companyId);

    List<Employee> findByBranchId(UUID branchId);

    List<Employee> findByEmploymentStatus(
            EmploymentStatus employmentStatus
    );

    List<Employee> findByCompanyIdAndEmploymentStatus(
            UUID companyId,
            EmploymentStatus employmentStatus
    );
}