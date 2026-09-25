package com.jewelvaulterp.payroll.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.payroll.dto.CreatePayrollRequest;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import com.jewelvaulterp.payroll.dto.PayrollResponse;
import com.jewelvaulterp.payroll.entity.Payroll;
import com.jewelvaulterp.payroll.entity.PayrollPaymentMethod;
import com.jewelvaulterp.payroll.entity.PayrollStatus;
import com.jewelvaulterp.payroll.repository.PayrollRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public PayrollService(
            PayrollRepository payrollRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.payrollRepository = payrollRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public List<PayrollResponse> getAll() {
        return payrollRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PayrollResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public List<PayrollResponse> getByCompany(UUID companyId) {
        return payrollRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PayrollResponse> getByEmployee(UUID employeeId) {
        return payrollRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PayrollResponse> getByStatus(PayrollStatus status) {
        return payrollRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PayrollResponse> getByPeriod(
            Integer year,
            Integer month
    ) {
        return payrollRepository
                .findByPayrollYearAndPayrollMonth(year, month)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PayrollResponse create(CreatePayrollRequest request) {

        Company company = companyRepository
                .findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found"));

        User employee = userRepository
                .findById(request.employeeId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found"));

        if (!employee.getCompany().getId()
                .equals(company.getId())) {

            throw new IllegalArgumentException(
                    "Employee does not belong to company");
        }

        if (payrollRepository
                .existsByEmployeeIdAndPayrollYearAndPayrollMonth(
                        request.employeeId(),
                        request.payrollYear(),
                        request.payrollMonth()
                )) {

            throw new IllegalArgumentException(
                    "Payroll already exists for employee and period");
        }

        Payroll payroll = new Payroll(
                company,
                employee,
                request.payrollYear(),
                request.payrollMonth(),
                request.basicSalary(),
                request.allowances(),
                request.deductions()
        );

        if (payroll.getNetSalary().signum() < 0) {
            throw new IllegalArgumentException(
                    "Deductions cannot exceed gross salary");
        }

        return toResponse(payrollRepository.save(payroll));
    }

    public PayrollResponse process(UUID id) {
        Payroll payroll = findById(id);

        if (payroll.getStatus() == PayrollStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cancelled payroll cannot be processed");
        }

        payroll.process();

        return toResponse(payroll);
    }

    public PayrollResponse markPaid(
            UUID id,
            LocalDate paymentDate,
            PayrollPaymentMethod paymentMethod,
            String referenceNumber
    ) {
        Payroll payroll = findById(id);

        if (payroll.getStatus() == PayrollStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cancelled payroll cannot be paid");
        }

        payroll.markPaid(
                paymentDate,
                paymentMethod,
                referenceNumber
        );

        return toResponse(payroll);
    }

    public PayrollResponse cancel(UUID id) {
        Payroll payroll = findById(id);

        if (payroll.getStatus() == PayrollStatus.PAID) {
            throw new IllegalArgumentException(
                    "Paid payroll cannot be cancelled");
        }

        payroll.cancel();

        return toResponse(payroll);
    }

    private Payroll findById(UUID id) {
        return payrollRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payroll not found"));
    }

    private PayrollResponse toResponse(Payroll payroll) {
        return new PayrollResponse(
                payroll.getId(),
                payroll.getCompany().getId(),
                payroll.getEmployee().getId(),
                payroll.getPayrollYear(),
                payroll.getPayrollMonth(),
                payroll.getBasicSalary(),
                payroll.getAllowances(),
                payroll.getDeductions(),
                payroll.getGrossSalary(),
                payroll.getNetSalary(),
                payroll.getPaymentDate(),
                payroll.getPaymentMethod(),
                payroll.getReferenceNumber(),
                payroll.getStatus(),
                payroll.getCreatedAt(),
                payroll.getUpdatedAt()
        );
    }
}