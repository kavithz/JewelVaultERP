package com.jewelvaulterp.jewellerytype.controller;

import com.jewelvaulterp.jewellerytype.dto.CreateJewelleryTypeRequest;
import com.jewelvaulterp.jewellerytype.dto.JewelleryTypeResponse;
import com.jewelvaulterp.jewellerytype.dto.UpdateJewelleryTypeRequest;
import com.jewelvaulterp.jewellerytype.service.JewelleryTypeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jewellery-types")
public class JewelleryTypeController {

    private final JewelleryTypeService jewelleryTypeService;

    public JewelleryTypeController(JewelleryTypeService jewelleryTypeService) {
        this.jewelleryTypeService = jewelleryTypeService;
    }

    @GetMapping
    public List<JewelleryTypeResponse> getJewelleryTypes() {
        return jewelleryTypeService.getAllJewelleryTypes();
    }

    @PostMapping
    public JewelleryTypeResponse createJewelleryType(
            @Valid @RequestBody CreateJewelleryTypeRequest request
    ) {
        return jewelleryTypeService.createJewelleryType(request);
    }

    @PutMapping("/{id}")
    public JewelleryTypeResponse updateJewelleryType(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJewelleryTypeRequest request
    ) {
        return jewelleryTypeService.updateJewelleryType(id, request);
    }

    @PatchMapping("/{id}/status")
    public JewelleryTypeResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return jewelleryTypeService.updateStatus(id, active);
    }
}