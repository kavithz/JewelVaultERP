package com.jewelvaulterp.tax.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.tax.dto.CreateTaxRequest;
import com.jewelvaulterp.tax.dto.TaxResponse;
import com.jewelvaulterp.tax.entity.Tax;
import com.jewelvaulterp.tax.entity.TaxType;
import com.jewelvaulterp.tax.repository.TaxRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaxService {

    private final TaxRepository taxRepository;
    private final CompanyRepository companyRepository;

    public TaxService(
            TaxRepository taxRepository,
            CompanyRepository companyRepository
    ) {
        this.taxRepository = taxRepository;
        this.companyRepository = companyRepository;
    }

    public List<TaxResponse> getAll() {
        return taxRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaxResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public List<TaxResponse> getByCompany(UUID companyId) {
        return taxRepository.findByCompanyId(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TaxResponse> getActiveByCompany(UUID companyId) {
        return taxRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TaxResponse> getByType(
            UUID companyId,
            TaxType taxType
    ) {
        return taxRepository
                .findByCompanyIdAndTaxType(companyId, taxType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaxResponse create(CreateTaxRequest request) {

        Company company = companyRepository
                .findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        Tax tax = new Tax(
                company,
                request.name(),
                request.code(),
                request.taxType(),
                request.rate(),
                request.description()
        );

        return toResponse(taxRepository.save(tax));
    }

    public TaxResponse activate(UUID id) {
        Tax tax = findById(id);
        tax.activate();
        return toResponse(tax);
    }

    public TaxResponse deactivate(UUID id) {
        Tax tax = findById(id);
        tax.deactivate();
        return toResponse(tax);
    }

    private Tax findById(UUID id) {
        return taxRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Tax not found"));
    }

    private TaxResponse toResponse(Tax tax) {
        return new TaxResponse(
                tax.getId(),
                tax.getCompany().getId(),
                tax.getName(),
                tax.getCode(),
                tax.getTaxType(),
                tax.getRate(),
                tax.getDescription(),
                tax.isActive(),
                tax.getCreatedAt(),
                tax.getUpdatedAt()
        );
    }
}