package com.jewelvaulterp.dashboard.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DashboardSummaryResponse(
        UUID companyId,
        BigDecimal totalSales,
        BigDecimal totalPurchases,
        BigDecimal totalExpenses,
        BigDecimal outstandingReceivables,
        BigDecimal outstandingPayables,
        BigDecimal inventoryValue,
        long activeUsers,
        long totalCustomers,
        long totalSuppliers,
        long totalProducts,
        long activeEmployees,
        BigDecimal salesToday,
        BigDecimal salesThisMonth,
        BigDecimal expensesToday,
        BigDecimal expensesThisMonth,
        BigDecimal payrollThisMonth,
        long unreadNotifications
) {
}