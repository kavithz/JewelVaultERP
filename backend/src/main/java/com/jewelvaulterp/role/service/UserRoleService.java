package com.jewelvaulterp.role.service;

import com.jewelvaulterp.role.dto.AssignRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserRoleService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<RoleResponse> getRolesForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<UUID> roleIds = userRepository.findUserRoleIds(user.getId());
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }

        return roleRepository.findAllById(roleIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponse assignRole(UUID userId, AssignRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Role role = roleRepository.findByIdAndCompanyId(request.roleId(), user.getCompany().getId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found for this company"));

        List<UUID> assignedRoleIds = userRepository.findUserRoleIds(user.getId());
        if (assignedRoleIds != null && assignedRoleIds.contains(role.getId())) {
            throw new IllegalArgumentException("Role is already assigned to this user");
        }

        userRepository.assignRoleToUser(user.getId(), role.getId());
        return toResponse(role);
    }

    public void removeRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Role role = roleRepository.findByIdAndCompanyId(roleId, user.getCompany().getId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found for this company"));

        List<UUID> assignedRoleIds = userRepository.findUserRoleIds(user.getId());
        if (assignedRoleIds == null || !assignedRoleIds.contains(roleId)) {
            throw new IllegalArgumentException("Role is not assigned to this user");
        }

        userRepository.removeRoleFromUser(user.getId(), role.getId());
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
