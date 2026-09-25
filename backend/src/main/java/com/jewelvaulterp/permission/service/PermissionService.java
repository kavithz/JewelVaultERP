package com.jewelvaulterp.permission.service;

import com.jewelvaulterp.permission.dto.AssignPermissionRequest;
import com.jewelvaulterp.permission.dto.PermissionResponse;
import com.jewelvaulterp.permission.entity.Permission;
import com.jewelvaulterp.permission.repository.PermissionRepository;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionService(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PermissionResponse getById(UUID id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
        return toResponse(permission);
    }

    public List<PermissionResponse> getByRole(UUID roleId, UUID companyId) {
        Role role = roleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found for this company"));

        return permissionRepository.findRolePermissions(role.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PermissionResponse assignToRole(UUID roleId, UUID companyId, AssignPermissionRequest request) {
        Role role = roleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found for this company"));

        Permission permission = permissionRepository.findById(request.permissionId())
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        List<Permission> assigned = permissionRepository.findRolePermissions(role.getId());
        boolean alreadyAssigned = assigned != null && assigned.stream().anyMatch(existing -> existing.getId().equals(permission.getId()));
        if (alreadyAssigned) {
            throw new IllegalArgumentException("Permission is already assigned to this role");
        }

        permissionRepository.assignPermissionToRole(role.getId(), permission.getId());
        return toResponse(permission);
    }

    public void removeFromRole(UUID roleId, UUID permissionId, UUID companyId) {
        Role role = roleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found for this company"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

        List<Permission> assigned = permissionRepository.findRolePermissions(role.getId());
        boolean exists = assigned != null && assigned.stream().anyMatch(existing -> existing.getId().equals(permission.getId()));
        if (!exists) {
            throw new IllegalArgumentException("Permission is not assigned to this role");
        }

        permissionRepository.removePermissionFromRole(role.getId(), permission.getId());
    }

    private PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription()
        );
    }
}
