package com.jewelvaulterp.jewellerytype.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.jewellerytype.dto.CreateJewelleryTypeRequest;
import com.jewelvaulterp.jewellerytype.dto.JewelleryTypeResponse;
import com.jewelvaulterp.jewellerytype.dto.UpdateJewelleryTypeRequest;
import com.jewelvaulterp.jewellerytype.entity.JewelleryType;
import com.jewelvaulterp.jewellerytype.repository.JewelleryTypeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JewelleryTypeService {

    private final JewelleryTypeRepository jewelleryTypeRepository;
    private final CompanyRepository companyRepository;

    public JewelleryTypeService(
            JewelleryTypeRepository jewelleryTypeRepository,
            CompanyRepository companyRepository
    ) {
        this.jewelleryTypeRepository = jewelleryTypeRepository;
        this.companyRepository = companyRepository;
    }

    public List<JewelleryTypeResponse> getAllJewelleryTypes() {
        return jewelleryTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public JewelleryTypeResponse createJewelleryType(
            CreateJewelleryTypeRequest request
    ) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (jewelleryTypeRepository.findByCompanyIdAndCode(
                request.companyId(),
                request.code()
        ).isPresent()) {
            throw new IllegalArgumentException("Jewellery type code already exists");
        }

        LocalDateTime now = LocalDateTime.now();

        JewelleryType jewelleryType = new JewelleryType(
                UUID.randomUUID(),
                company,
                request.name(),
                request.code(),
                request.description(),
                true,
                now,
                now
        );

        return toResponse(jewelleryTypeRepository.save(jewelleryType));
    }

    public JewelleryTypeResponse updateJewelleryType(
            UUID id,
            UpdateJewelleryTypeRequest request
    ) {
        JewelleryType jewelleryType = jewelleryTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jewellery type not found"));

        jewelleryTypeRepository.findByCompanyIdAndCode(
                        jewelleryType.getCompany().getId(),
                        request.code()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Jewellery type code already exists");
                });

        jewelleryType.update(
                request.name(),
                request.code(),
                request.description()
        );

        return toResponse(jewelleryTypeRepository.save(jewelleryType));
    }

    public JewelleryTypeResponse updateStatus(
            UUID id,
            boolean active
    ) {
        JewelleryType jewelleryType = jewelleryTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jewellery type not found"));

        jewelleryType.setActive(active);

        return toResponse(jewelleryTypeRepository.save(jewelleryType));
    }

    private JewelleryTypeResponse toResponse(JewelleryType jewelleryType) {
        return new JewelleryTypeResponse(
                jewelleryType.getId(),
                jewelleryType.getCompany().getId(),
                jewelleryType.getName(),
                jewelleryType.getCode(),
                jewelleryType.getDescription(),
                jewelleryType.isActive(),
                jewelleryType.getCreatedAt(),
                jewelleryType.getUpdatedAt()
        );
    }
}