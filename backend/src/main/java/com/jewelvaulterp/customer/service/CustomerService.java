package com.jewelvaulterp.customer.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.customer.dto.CreateCustomerRequest;
import com.jewelvaulterp.customer.dto.CustomerResponse;
import com.jewelvaulterp.customer.dto.UpdateCustomerRequest;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            CompanyRepository companyRepository
    ) {
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
    }

    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getCustomer(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        return toResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(
            CreateCustomerRequest request
    ) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        if (customerRepository.findByCompanyIdAndCode(
                request.companyId(),
                request.code()
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "Customer code already exists for this company"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Customer customer = new Customer(
                UUID.randomUUID(),
                company,
                request.name(),
                request.code(),
                request.phone(),
                request.email(),
                request.address(),
                request.taxNumber(),
                true,
                now,
                now
        );

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(
            UUID id,
            UpdateCustomerRequest request
    ) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        customerRepository.findByCompanyIdAndCode(
                        customer.getCompany().getId(),
                        request.code()
                )
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Customer code already exists for this company"
                        );
                    }
                });

        customer.update(
                request.name(),
                request.code(),
                request.phone(),
                request.email(),
                request.address(),
                request.taxNumber()
        );

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateStatus(
            UUID id,
            boolean active
    ) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        customer.setActive(active);

        return toResponse(customerRepository.save(customer));
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCompany().getId(),
                customer.getName(),
                customer.getCode(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getTaxNumber(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}