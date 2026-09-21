package com.jewelvaulterp.purity.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.purity.dto.CreatePurityRequest;
import com.jewelvaulterp.purity.dto.PurityResponse;
import com.jewelvaulterp.purity.dto.UpdatePurityRequest;
import com.jewelvaulterp.purity.entity.Purity;
import com.jewelvaulterp.purity.repository.PurityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PurityService {

    private final PurityRepository purityRepository;
    private final CompanyRepository companyRepository;

    public PurityService(
            PurityRepository purityRepository,
            CompanyRepository companyRepository
    ) {
        this.purityRepository = purityRepository;
        this.companyRepository = companyRepository;
    }

    public List<PurityResponse> getAllPurities() {
        return purityRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PurityResponse createPurity(CreatePurityRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (purityRepository.findByCompanyIdAndCode(
                request.companyId(),
                request.code()
        ).isPresent()) {
            throw new IllegalArgumentException("Purity code already exists");
        }

        LocalDateTime now = LocalDateTime.now();

        Purity purity = new Purity(
                UUID.randomUUID(),
                company,
                request.name(),
                request.code(),
                request.fineness(),
                request.description(),
                true,
                now,
                now
        );

        return toResponse(purityRepository.save(purity));
    }

    public PurityResponse updatePurity(
            UUID id,
            UpdatePurityRequest request
    ) {
        Purity purity = purityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purity not found"));

        purityRepository.findByCompanyIdAndCode(
                        purity.getCompany().getId(),
                        request.code()
                )
                .filter(existingPurity -> !existingPurity.getId().equals(id))
                .ifPresent(existingPurity -> {
                    throw new IllegalArgumentException("Purity code already exists");
                });

        purity.update(
                request.name(),
                request.code(),
                request.fineness(),
                request.description()
        );

        return toResponse(purityRepository.save(purity));
    }

    public PurityResponse updateStatus(
            UUID id,
            boolean active
    ) {
        Purity purity = purityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purity not found"));

        purity.setActive(active);

        return toResponse(purityRepository.save(purity));
    }

    private PurityResponse toResponse(Purity purity) {
        return new PurityResponse(
                purity.getId(),
                purity.getCompany().getId(),
                purity.getName(),
                purity.getCode(),
                purity.getFineness(),
                purity.getDescription(),
                purity.isActive(),
                purity.getCreatedAt(),
                purity.getUpdatedAt()
        );
    }
}