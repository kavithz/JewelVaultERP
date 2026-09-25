package com.jewelvaulterp.role.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.role.dto.CreateRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.dto.UpdateRoleRequest;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public RoleService(
            RoleRepository roleRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public RoleResponse create(CreateRoleRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        String normalizedName = normalizeName(request.name());
        if (roleRepository.findByCompanyIdAndNameIgnoreCase(company.getId(), normalizedName).isPresent()) {
            throw new IllegalArgumentException("Role name is duplicate for this company");
        }

        Role role = new Role(
                UUID.randomUUID(),
                company,
                normalizedName,
                request.description() == null ? null : request.description().trim(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return toResponse(roleRepository.save(role));
    }

    public RoleResponse getById(UUID id, UUID companyId) {
        return toResponse(findByCompanyRole(id, companyId));
    }

    public List<RoleResponse> getByCompany(UUID companyId) {
        validateCompanyExists(companyId);
        return roleRepository.findByCompanyIdOrderByNameAsc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponse update(UUID id, UUID companyId, UpdateRoleRequest request) {
        Role role = findByCompanyRole(id, companyId);

        String proposedName = request.name() == null ? role.getName() : normalizeName(request.name());
        if (!proposedName.equalsIgnoreCase(role.getName())
                && roleRepository.findByCompanyIdAndNameIgnoreCase(companyId, proposedName).isPresent()) {
            throw new IllegalArgumentException("Role name is duplicate for this company");
        }

        String description = request.description() == null ? role.getDescription() : request.description().trim();
        Boolean active = request.active();

        role.update(proposedName, description, active == null ? role.isActive() : active);
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse activate(UUID id, UUID companyId) {
        Role role = findByCompanyRole(id, companyId);
        role.activate();
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse deactivate(UUID id, UUID companyId) {
        Role role = findByCompanyRole(id, companyId);
        role.deactivate();
        return toResponse(roleRepository.save(role));
    }

    public void delete(UUID id, UUID companyId) {
        Role role = findByCompanyRole(id, companyId);

        boolean assigned = userRepository.findByCompanyId(companyId).stream()
                .map(user -> userRepository.findUserRoleIds(user.getId()))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(roleId -> Objects.equals(roleId, id));

        if (assigned) {
            throw new IllegalArgumentException("Role is assigned to users and cannot be deleted");
        }

        roleRepository.delete(role);
    }

    private Role findByCompanyRole(UUID id, UUID companyId) {
        return roleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found for this company"));
    }

    private void validateCompanyExists(UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    private String normalizeName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role name is required");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Role name is required");
        }
        return trimmed;
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getCompany().getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
