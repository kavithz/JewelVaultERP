package com.jewelvaulterp.supplier.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.supplier.dto.CreateSupplierRequest;
import com.jewelvaulterp.supplier.dto.SupplierResponse;
import com.jewelvaulterp.supplier.dto.UpdateSupplierRequest;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            CompanyRepository companyRepository
    ) {
        this.supplierRepository = supplierRepository;
        this.companyRepository = companyRepository;
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SupplierResponse createSupplier(
            CreateSupplierRequest request
    ) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        if (supplierRepository.findByCompanyIdAndCode(
                request.companyId(),
                request.code()
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "Supplier code already exists"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Supplier supplier = new Supplier(
                UUID.randomUUID(),
                company,
                request.name(),
                request.code(),
                request.contactPerson(),
                request.phone(),
                request.email(),
                request.address(),
                request.taxNumber(),
                true,
                now,
                now
        );

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse updateSupplier(
            UUID id,
            UpdateSupplierRequest request
    ) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Supplier not found"));

        supplierRepository.findByCompanyIdAndCode(
                        supplier.getCompany().getId(),
                        request.code()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Supplier code already exists"
                    );
                });

        supplier.update(
                request.name(),
                request.code(),
                request.contactPerson(),
                request.phone(),
                request.email(),
                request.address(),
                request.taxNumber()
        );

        return toResponse(supplierRepository.save(supplier));
    }

    public SupplierResponse updateStatus(
            UUID id,
            boolean active
    ) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Supplier not found"));

        supplier.setActive(active);

        return toResponse(supplierRepository.save(supplier));
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getCompany().getId(),
                supplier.getName(),
                supplier.getCode(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getTaxNumber(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}