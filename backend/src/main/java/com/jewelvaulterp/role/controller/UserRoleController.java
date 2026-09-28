package com.jewelvaulterp.role.controller;

import com.jewelvaulterp.role.dto.AssignRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.service.UserRoleService;
import com.jewelvaulterp.auth.service.CurrentUserAccess;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    private final UserRoleService userRoleService;
    private final CurrentUserAccess currentUserAccess;

    public UserRoleController(UserRoleService userRoleService, CurrentUserAccess currentUserAccess) {
        this.userRoleService = userRoleService;
        this.currentUserAccess = currentUserAccess;
    }

    @GetMapping("/user/{userId}")
    public List<RoleResponse> getRolesForUser(@PathVariable UUID userId) {
        return userRoleService.getRolesForUser(currentUserAccess.requireUserInCompany(userId));
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        return userRoleService.assignRole(currentUserAccess.requireUserInCompany(userId), request);
    }

    @DeleteMapping("/user/{userId}/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRole(@PathVariable UUID userId, @PathVariable UUID roleId) {
        userRoleService.removeRole(currentUserAccess.requireUserInCompany(userId), roleId);
    }
}
