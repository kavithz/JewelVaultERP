package com.jewelvaulterp.dashboard.dto;

public record DashboardSummaryResponse(
        long totalCompanies,
        long totalUsers,
        long activeUsers,
        long inactiveUsers
) {
}