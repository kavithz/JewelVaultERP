package com.jewelvaulterp.dashboard.controller;

import com.jewelvaulterp.dashboard.dto.DashboardSummaryResponse;
import com.jewelvaulterp.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/{companyId}")
    public DashboardSummaryResponse getSummary(@PathVariable UUID companyId) {
        return dashboardService.getSummary(companyId);
    }

    @GetMapping("/summary/{companyId}")
    public DashboardSummaryResponse getSummaryByPath(@PathVariable UUID companyId) {
        return dashboardService.getSummary(companyId);
    }
}
