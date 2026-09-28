package com.jewelvaulterp.role.controller;

import com.jewelvaulterp.role.dto.CreateRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.dto.UpdateRoleRequest;
import com.jewelvaulterp.role.service.RoleService;
import com.jewelvaulterp.auth.service.CurrentUserAccess;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final CurrentUserAccess currentUserAccess;

    public RoleController(RoleService roleService, CurrentUserAccess currentUserAccess) {
        this.roleService = roleService;
        this.currentUserAccess = currentUserAccess;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        currentUserAccess.requireCompany(request.companyId());
        return roleService.create(request);
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable UUID id, @RequestParam UUID companyId) {
        return roleService.getById(id, currentUserAccess.requireCompany(companyId));
    }

    @GetMapping("/company/{companyId}")
    public List<RoleResponse> getByCompany(@PathVariable UUID companyId) {
        return roleService.getByCompany(currentUserAccess.requireCompany(companyId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse update(
            @PathVariable UUID id,
            @RequestParam UUID companyId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return roleService.update(id, currentUserAccess.requireCompany(companyId), request);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse activate(@PathVariable UUID id, @RequestParam UUID companyId) {
        return roleService.activate(id, currentUserAccess.requireCompany(companyId));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public RoleResponse deactivate(@PathVariable UUID id, @RequestParam UUID companyId) {
        return roleService.deactivate(id, currentUserAccess.requireCompany(companyId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @RequestParam UUID companyId) {
        roleService.delete(id, currentUserAccess.requireCompany(companyId));
    }
}
