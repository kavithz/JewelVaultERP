package com.jewelvaulterp.payroll.repository;

import com.jewelvaulterp.payroll.entity.Payroll;
import com.jewelvaulterp.payroll.entity.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayrollRepository
        extends JpaRepository<Payroll, UUID> {

    List<Payroll> findByCompanyId(UUID companyId);

    List<Payroll> findByEmployeeId(UUID employeeId);

    List<Payroll> findByStatus(PayrollStatus status);

    List<Payroll> findByCompanyIdAndStatus(
            UUID companyId,
            PayrollStatus status
    );

    List<Payroll> findByPayrollYearAndPayrollMonth(
            Integer payrollYear,
            Integer payrollMonth
    );

    boolean existsByEmployeeIdAndPayrollYearAndPayrollMonth(
            UUID employeeId,
            Integer payrollYear,
            Integer payrollMonth
    );
}