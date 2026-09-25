package com.jewelvaulterp.permission.controller;

import com.jewelvaulterp.permission.dto.AssignPermissionRequest;
import com.jewelvaulterp.permission.dto.PermissionResponse;
import com.jewelvaulterp.permission.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<PermissionResponse> getAll() {
        return permissionService.getAll();
    }

    @GetMapping("/{id}")
    public PermissionResponse getById(@PathVariable UUID id) {
        return permissionService.getById(id);
    }

    @GetMapping("/role/{roleId}")
    public List<PermissionResponse> getByRole(
            @PathVariable UUID roleId,
            @RequestParam UUID companyId
    ) {
        return permissionService.getByRole(roleId, companyId);
    }

    @PostMapping("/role/{roleId}")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse assignToRole(
            @PathVariable UUID roleId,
            @RequestParam UUID companyId,
            @Valid @RequestBody AssignPermissionRequest request
    ) {
        return permissionService.assignToRole(roleId, companyId, request);
    }

    @DeleteMapping("/role/{roleId}/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromRole(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId,
            @RequestParam UUID companyId
    ) {
        permissionService.removeFromRole(roleId, permissionId, companyId);
    }
}
