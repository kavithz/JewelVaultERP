package com.jewelvaulterp.purity.controller;

import com.jewelvaulterp.purity.dto.CreatePurityRequest;
import com.jewelvaulterp.purity.dto.PurityResponse;
import com.jewelvaulterp.purity.dto.UpdatePurityRequest;
import com.jewelvaulterp.purity.service.PurityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purities")
public class PurityController {

    private final PurityService purityService;

    public PurityController(PurityService purityService) {
        this.purityService = purityService;
    }

    @GetMapping
    public List<PurityResponse> getPurities() {
        return purityService.getAllPurities();
    }

    @PostMapping
    public PurityResponse createPurity(
            @Valid @RequestBody CreatePurityRequest request
    ) {
        return purityService.createPurity(request);
    }

    @PutMapping("/{id}")
    public PurityResponse updatePurity(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurityRequest request
    ) {
        return purityService.updatePurity(id, request);
    }

    @PatchMapping("/{id}/status")
    public PurityResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return purityService.updateStatus(id, active);
    }
}