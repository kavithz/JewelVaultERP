package com.jewelvaulterp.gemstone.controller;

import com.jewelvaulterp.gemstone.dto.CreateGemstoneRequest;
import com.jewelvaulterp.gemstone.dto.GemstoneResponse;
import com.jewelvaulterp.gemstone.dto.UpdateGemstoneRequest;
import com.jewelvaulterp.gemstone.service.GemstoneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gemstones")
public class GemstoneController {

    private final GemstoneService gemstoneService;

    public GemstoneController(GemstoneService gemstoneService) {
        this.gemstoneService = gemstoneService;
    }

    @GetMapping
    public List<GemstoneResponse> getGemstones() {
        return gemstoneService.getAllGemstones();
    }

    @PostMapping
    public GemstoneResponse createGemstone(
            @Valid @RequestBody CreateGemstoneRequest request
    ) {
        return gemstoneService.createGemstone(request);
    }

    @PutMapping("/{id}")
    public GemstoneResponse updateGemstone(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGemstoneRequest request
    ) {
        return gemstoneService.updateGemstone(id, request);
    }

    @PatchMapping("/{id}/status")
    public GemstoneResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return gemstoneService.updateStatus(id, active);
    }
}