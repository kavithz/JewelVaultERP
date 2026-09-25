package com.jewelvaulterp.employee.service;

import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.branch.repository.BranchRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.employee.dto.CreateEmployeeRequest;
import com.jewelvaulterp.employee.dto.EmployeeResponse;
import com.jewelvaulterp.employee.entity.Employee;
import com.jewelvaulterp.employee.entity.EmploymentStatus;
import com.jewelvaulterp.employee.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            CompanyRepository companyRepository,
            BranchRepository branchRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.branchRepository = branchRepository;
    }

    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public List<EmployeeResponse> getByCompany(UUID companyId) {
        return employeeRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EmployeeResponse> getByBranch(UUID branchId) {
        return employeeRepository.findByBranchId(branchId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EmployeeResponse> getByStatus(
            EmploymentStatus status
    ) {
        return employeeRepository.findByEmploymentStatus(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EmployeeResponse create(CreateEmployeeRequest request) {

        Company company = companyRepository
                .findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        Branch branch = null;

        if (request.branchId() != null) {
            branch = branchRepository
                    .findById(request.branchId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Branch not found"));

            if (!branch.getCompany().getId()
                    .equals(company.getId())) {

                throw new IllegalArgumentException(
                        "Branch does not belong to company");
            }
        }

        Employee employee = new Employee(
                company,
                branch,
                request.employeeNumber(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.address(),
                request.department(),
                request.designation(),
                request.joiningDate()
        );

        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse updateStatus(
            UUID id,
            EmploymentStatus status
    ) {
        Employee employee = findById(id);
        employee.updateStatus(status);
        return toResponse(employee);
    }

    public EmployeeResponse setExitDate(
            UUID id,
            LocalDate exitDate
    ) {
        Employee employee = findById(id);
        employee.setExitDate(exitDate);
        return toResponse(employee);
    }

    private Employee findById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee not found"));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getCompany().getId(),
                employee.getBranch() != null
                        ? employee.getBranch().getId()
                        : null,
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getAddress(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getEmploymentStatus(),
                employee.getJoiningDate(),
                employee.getExitDate(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}