package com.jewelvaulterp.financial.controller;

import com.jewelvaulterp.financial.dto.CashFlowSummaryResponse;
import com.jewelvaulterp.financial.dto.ExpenseSummaryResponse;
import com.jewelvaulterp.financial.dto.FinancialSummaryResponse;
import com.jewelvaulterp.financial.dto.PayablesSummaryResponse;
import com.jewelvaulterp.financial.dto.PurchaseSummaryResponse;
import com.jewelvaulterp.financial.dto.ReceivablesSummaryResponse;
import com.jewelvaulterp.financial.dto.SalesSummaryResponse;
import com.jewelvaulterp.financial.dto.TaxSummaryResponse;
import com.jewelvaulterp.financial.service.FinancialReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/financial/reports")
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    public FinancialReportController(
            FinancialReportService financialReportService
    ) {
        this.financialReportService = financialReportService;
    }

    @GetMapping("/sales/{companyId}")
    public SalesSummaryResponse getSalesSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getSalesSummary(companyId);
    }

    @GetMapping("/purchases/{companyId}")
    public PurchaseSummaryResponse getPurchaseSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getPurchaseSummary(companyId);
    }

    @GetMapping("/expenses/{companyId}")
    public ExpenseSummaryResponse getExpenseSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getExpenseSummary(companyId);
    }

    @GetMapping("/cash-flow/{companyId}")
    public CashFlowSummaryResponse getCashFlowSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getCashFlowSummary(companyId);
    }

    @GetMapping("/receivables/{companyId}")
    public ReceivablesSummaryResponse getReceivablesSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getReceivablesSummary(companyId);
    }

    @GetMapping("/payables/{companyId}")
    public PayablesSummaryResponse getPayablesSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getPayablesSummary(companyId);
    }

    @GetMapping("/tax/{companyId}")
    public TaxSummaryResponse getTaxSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getTaxSummary(companyId);
    }

    @GetMapping("/summary/{companyId}")
    public FinancialSummaryResponse getFinancialSummary(
            @PathVariable UUID companyId
    ) {
        return financialReportService.getFinancialSummary(companyId);
    }
}
