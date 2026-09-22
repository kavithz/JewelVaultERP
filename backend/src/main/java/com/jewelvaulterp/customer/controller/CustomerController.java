package com.jewelvaulterp.customer.controller;

import com.jewelvaulterp.customer.dto.CreateCustomerRequest;
import com.jewelvaulterp.customer.dto.CustomerResponse;
import com.jewelvaulterp.customer.dto.UpdateCustomerRequest;
import com.jewelvaulterp.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> getCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(
            @PathVariable UUID id
    ) {
        return customerService.getCustomer(id);
    }

    @PostMapping
    public CustomerResponse createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return customerService.createCustomer(request);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return customerService.updateCustomer(id, request);
    }

    @PatchMapping("/{id}/status")
    public CustomerResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return customerService.updateStatus(id, active);
    }
}