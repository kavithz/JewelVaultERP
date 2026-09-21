package com.jewelvaulterp.branch.service;

import com.jewelvaulterp.branch.dto.BranchResponse;
import com.jewelvaulterp.branch.dto.CreateBranchRequest;
import com.jewelvaulterp.branch.dto.UpdateBranchRequest;
import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.branch.repository.BranchRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;

    public BranchService(
            BranchRepository branchRepository,
            CompanyRepository companyRepository
    ) {
        this.branchRepository = branchRepository;
        this.companyRepository = companyRepository;
    }

    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BranchResponse createBranch(CreateBranchRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (branchRepository.findByCompanyIdAndCode(
                request.companyId(),
                request.code()
        ).isPresent()) {
            throw new IllegalArgumentException("Branch code already exists");
        }

        LocalDateTime now = LocalDateTime.now();

        Branch branch = new Branch(
                UUID.randomUUID(),
                company,
                request.name(),
                request.code(),
                request.address(),
                request.city(),
                request.countryCode(),
                request.phone(),
                request.email(),
                true,
                now,
                now
        );

        return toResponse(branchRepository.save(branch));
    }

    public BranchResponse updateBranch(
            UUID id,
            UpdateBranchRequest request
    ) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        branchRepository.findByCompanyIdAndCode(
                        branch.getCompany().getId(),
                        request.code()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Branch code already exists");
                });

        branch.update(
                request.name(),
                request.code(),
                request.address(),
                request.city(),
                request.countryCode(),
                request.phone(),
                request.email()
        );

        return toResponse(branchRepository.save(branch));
    }

    public BranchResponse updateStatus(
            UUID id,
            boolean active
    ) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        branch.setActive(active);

        return toResponse(branchRepository.save(branch));
    }

    private BranchResponse toResponse(Branch branch) {

        return new BranchResponse(
                branch.getId(),
                branch.getCompany().getId(),
                branch.getName(),
                branch.getCode(),
                branch.getAddress(),
                branch.getCity(),
                branch.getCountryCode(),
                branch.getPhone(),
                branch.getEmail(),
                branch.isActive(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }
}