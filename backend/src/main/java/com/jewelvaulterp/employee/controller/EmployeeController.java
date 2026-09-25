package com.jewelvaulterp.employee.controller;

import com.jewelvaulterp.employee.dto.CreateEmployeeRequest;
import com.jewelvaulterp.employee.dto.EmployeeResponse;
import com.jewelvaulterp.employee.entity.EmploymentStatus;
import com.jewelvaulterp.employee.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> getAll() {
        return employeeService.getAll();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(
            @PathVariable UUID id
    ) {
        return employeeService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<EmployeeResponse> getByCompany(
            @PathVariable UUID companyId
    ) {
        return employeeService.getByCompany(companyId);
    }

    @GetMapping("/branch/{branchId}")
    public List<EmployeeResponse> getByBranch(
            @PathVariable UUID branchId
    ) {
        return employeeService.getByBranch(branchId);
    }

    @GetMapping("/status/{status}")
    public List<EmployeeResponse> getByStatus(
            @PathVariable EmploymentStatus status
    ) {
        return employeeService.getByStatus(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return employeeService.create(request);
    }

    @PatchMapping("/{id}/status/{status}")
    public EmployeeResponse updateStatus(
            @PathVariable UUID id,
            @PathVariable EmploymentStatus status
    ) {
        return employeeService.updateStatus(id, status);
    }

    @PatchMapping("/{id}/exit-date")
    public EmployeeResponse setExitDate(
            @PathVariable UUID id,
            @RequestParam LocalDate exitDate
    ) {
        return employeeService.setExitDate(id, exitDate);
    }
}