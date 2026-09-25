package com.jewelvaulterp.role.controller;

import com.jewelvaulterp.role.dto.CreateRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.dto.UpdateRoleRequest;
import com.jewelvaulterp.role.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
        return roleService.create(request);
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable UUID id, @RequestParam UUID companyId) {
        return roleService.getById(id, companyId);
    }

    @GetMapping("/company/{companyId}")
    public List<RoleResponse> getByCompany(@PathVariable UUID companyId) {
        return roleService.getByCompany(companyId);
    }

    @PatchMapping("/{id}")
    public RoleResponse update(
            @PathVariable UUID id,
            @RequestParam UUID companyId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return roleService.update(id, companyId, request);
    }

    @PatchMapping("/{id}/activate")
    public RoleResponse activate(@PathVariable UUID id, @RequestParam UUID companyId) {
        return roleService.activate(id, companyId);
    }

    @PatchMapping("/{id}/deactivate")
    public RoleResponse deactivate(@PathVariable UUID id, @RequestParam UUID companyId) {
        return roleService.deactivate(id, companyId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @RequestParam UUID companyId) {
        roleService.delete(id, companyId);
    }
}
