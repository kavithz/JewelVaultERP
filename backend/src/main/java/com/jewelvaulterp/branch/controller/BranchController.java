package com.jewelvaulterp.branch.controller;

import com.jewelvaulterp.branch.dto.BranchResponse;
import com.jewelvaulterp.branch.dto.CreateBranchRequest;
import com.jewelvaulterp.branch.dto.UpdateBranchRequest;
import com.jewelvaulterp.branch.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public List<BranchResponse> getBranches() {
        return branchService.getAllBranches();
    }

    @PostMapping
    public BranchResponse createBranch(
            @Valid @RequestBody CreateBranchRequest request
    ) {
        return branchService.createBranch(request);
    }

    @PutMapping("/{id}")
    public BranchResponse updateBranch(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBranchRequest request
    ) {
        return branchService.updateBranch(id, request);
    }

    @PatchMapping("/{id}/status")
    public BranchResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return branchService.updateStatus(id, active);
    }
}