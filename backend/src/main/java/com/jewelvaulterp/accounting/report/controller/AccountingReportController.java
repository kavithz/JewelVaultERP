package com.jewelvaulterp.accounting.report.controller;

import com.jewelvaulterp.accounting.report.dto.AccountBalanceResponse;
import com.jewelvaulterp.accounting.report.dto.BalanceSheetResponse;
import com.jewelvaulterp.accounting.report.dto.ProfitLossResponse;
import com.jewelvaulterp.accounting.report.dto.TrialBalanceResponse;
import com.jewelvaulterp.accounting.report.service.AccountingReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounting/reports")
public class AccountingReportController {

    private final AccountingReportService accountingReportService;

    public AccountingReportController(
            AccountingReportService accountingReportService
    ) {
        this.accountingReportService = accountingReportService;
    }

    @GetMapping("/accounts/{companyId}")
    public List<AccountBalanceResponse> getAccountBalances(
            @PathVariable UUID companyId
    ) {
        return accountingReportService
                .getAccountBalances(companyId);
    }

    @GetMapping("/trial-balance/{companyId}")
    public TrialBalanceResponse getTrialBalance(
            @PathVariable UUID companyId
    ) {
        return accountingReportService
                .getTrialBalance(companyId);
    }

    @GetMapping("/profit-loss/{companyId}")
    public ProfitLossResponse getProfitAndLoss(
            @PathVariable UUID companyId
    ) {
        return accountingReportService
                .getProfitAndLoss(companyId);
    }

    @GetMapping("/balance-sheet/{companyId}")
    public BalanceSheetResponse getBalanceSheet(
            @PathVariable UUID companyId
    ) {
        return accountingReportService
                .getBalanceSheet(companyId);
    }
}