package com.jewelvaulterp.role.controller;

import com.jewelvaulterp.role.dto.AssignRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.service.UserRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @GetMapping("/user/{userId}")
    public List<RoleResponse> getRolesForUser(@PathVariable UUID userId) {
        return userRoleService.getRolesForUser(userId);
    }

    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        return userRoleService.assignRole(userId, request);
    }

    @DeleteMapping("/user/{userId}/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRole(@PathVariable UUID userId, @PathVariable UUID roleId) {
        userRoleService.removeRole(userId, roleId);
    }
}
