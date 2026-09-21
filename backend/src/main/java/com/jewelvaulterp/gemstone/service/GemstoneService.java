package com.jewelvaulterp.gemstone.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.gemstone.dto.CreateGemstoneRequest;
import com.jewelvaulterp.gemstone.dto.GemstoneResponse;
import com.jewelvaulterp.gemstone.dto.UpdateGemstoneRequest;
import com.jewelvaulterp.gemstone.entity.Gemstone;
import com.jewelvaulterp.gemstone.repository.GemstoneRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GemstoneService {

    private final GemstoneRepository gemstoneRepository;
    private final CompanyRepository companyRepository;

    public GemstoneService(
            GemstoneRepository gemstoneRepository,
            CompanyRepository companyRepository
    ) {
        this.gemstoneRepository = gemstoneRepository;
        this.companyRepository = companyRepository;
    }

    public List<GemstoneResponse> getAllGemstones() {
        return gemstoneRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GemstoneResponse createGemstone(
            CreateGemstoneRequest request
    ) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (gemstoneRepository.findByCompanyIdAndCode(
                request.companyId(),
                request.code()
        ).isPresent()) {
            throw new IllegalArgumentException("Gemstone code already exists");
        }

        LocalDateTime now = LocalDateTime.now();

        Gemstone gemstone = new Gemstone(
                UUID.randomUUID(),
                company,
                request.name(),
                request.code(),
                request.category(),
                request.color(),
                request.description(),
                true,
                now,
                now
        );

        return toResponse(gemstoneRepository.save(gemstone));
    }

    public GemstoneResponse updateGemstone(
            UUID id,
            UpdateGemstoneRequest request
    ) {
        Gemstone gemstone = gemstoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gemstone not found"));

        gemstoneRepository.findByCompanyIdAndCode(
                        gemstone.getCompany().getId(),
                        request.code()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Gemstone code already exists");
                });

        gemstone.update(
                request.name(),
                request.code(),
                request.category(),
                request.color(),
                request.description()
        );

        return toResponse(gemstoneRepository.save(gemstone));
    }

    public GemstoneResponse updateStatus(
            UUID id,
            boolean active
    ) {
        Gemstone gemstone = gemstoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gemstone not found"));

        gemstone.setActive(active);

        return toResponse(gemstoneRepository.save(gemstone));
    }

    private GemstoneResponse toResponse(Gemstone gemstone) {
        return new GemstoneResponse(
                gemstone.getId(),
                gemstone.getCompany().getId(),
                gemstone.getName(),
                gemstone.getCode(),
                gemstone.getCategory(),
                gemstone.getColor(),
                gemstone.getDescription(),
                gemstone.isActive(),
                gemstone.getCreatedAt(),
                gemstone.getUpdatedAt()
        );
    }
}