package com.jewelvaulterp.accounting.report.service;

import com.jewelvaulterp.accounting.entity.Account;
import com.jewelvaulterp.accounting.entity.AccountType;
import com.jewelvaulterp.accounting.entity.JournalEntryLine;
import com.jewelvaulterp.accounting.entity.JournalEntryStatus;
import com.jewelvaulterp.accounting.report.dto.AccountBalanceResponse;
import com.jewelvaulterp.accounting.report.dto.BalanceSheetResponse;
import com.jewelvaulterp.accounting.report.dto.ProfitLossResponse;
import com.jewelvaulterp.accounting.report.dto.TrialBalanceResponse;
import com.jewelvaulterp.accounting.repository.AccountRepository;
import com.jewelvaulterp.accounting.repository.JournalEntryLineRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class AccountingReportService {

    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    public AccountingReportService(
            AccountRepository accountRepository,
            JournalEntryLineRepository journalEntryLineRepository
    ) {
        this.accountRepository = accountRepository;
        this.journalEntryLineRepository = journalEntryLineRepository;
    }

    public List<AccountBalanceResponse> getAccountBalances(
            UUID companyId
    ) {
        List<JournalEntryLine> lines =
                getPostedLines(companyId);

        Map<UUID, BigDecimal> debits = new HashMap<>();
        Map<UUID, BigDecimal> credits = new HashMap<>();

        for (JournalEntryLine line : lines) {
            UUID accountId = line.getAccount().getId();

            BigDecimal existingDebit = debits.get(accountId);
            BigDecimal nextDebit = line.getDebitAmount();
            debits.put(
                    accountId,
                    (existingDebit == null)
                            ? nextDebit
                            : existingDebit.add(nextDebit)
            );

            BigDecimal existingCredit = credits.get(accountId);
            BigDecimal nextCredit = line.getCreditAmount();
            credits.put(
                    accountId,
                    (existingCredit == null)
                            ? nextCredit
                            : existingCredit.add(nextCredit)
            );
        }

        return accountRepository.findByCompanyId(companyId)
                .stream()
                .map(account -> toAccountBalance(
                        account,
                        debits.getOrDefault(
                                account.getId(),
                                BigDecimal.ZERO
                        ),
                        credits.getOrDefault(
                                account.getId(),
                                BigDecimal.ZERO
                        )
                ))
                .toList();
    }

    public TrialBalanceResponse getTrialBalance(
            UUID companyId
    ) {
        List<AccountBalanceResponse> accounts =
                getAccountBalances(companyId);

        BigDecimal totalDebits = accounts.stream()
                .map(account -> account.debitTotal() == null
                        ? BigDecimal.ZERO
                        : account.debitTotal())
                .reduce(BigDecimal.ZERO, (runningTotal, value) ->
                        runningTotal.add(value == null ? BigDecimal.ZERO : value));

        BigDecimal totalCredits = accounts.stream()
                .map(account -> account.creditTotal() == null
                        ? BigDecimal.ZERO
                        : account.creditTotal())
                .reduce(BigDecimal.ZERO, (runningTotal, value) ->
                        runningTotal.add(value == null ? BigDecimal.ZERO : value));

        return new TrialBalanceResponse(
                accounts,
                totalDebits,
                totalCredits
        );
    }

    public ProfitLossResponse getProfitAndLoss(
            UUID companyId
    ) {
        List<AccountBalanceResponse> accounts =
                getAccountBalances(companyId);

        BigDecimal revenue = accounts.stream()
                .filter(account ->
                        account.accountType() == AccountType.REVENUE)
                .map(account -> account.balance() == null
                        ? BigDecimal.ZERO
                        : account.balance())
                .reduce(BigDecimal.ZERO, (runningTotal, value) ->
                        runningTotal.add(value == null ? BigDecimal.ZERO : value));

        BigDecimal expenses = accounts.stream()
                .filter(account ->
                        account.accountType() == AccountType.EXPENSE)
                .map(account -> account.balance() == null
                        ? BigDecimal.ZERO
                        : account.balance())
                .reduce(BigDecimal.ZERO, (runningTotal, value) ->
                        runningTotal.add(value == null ? BigDecimal.ZERO : value));

        BigDecimal netProfit = revenue.subtract(expenses);

        return new ProfitLossResponse(
                revenue,
                expenses,
                netProfit
        );
    }

    public BalanceSheetResponse getBalanceSheet(
            UUID companyId
    ) {
        List<AccountBalanceResponse> accounts =
                getAccountBalances(companyId);

        BigDecimal assets = sumByType(
                accounts,
                AccountType.ASSET
        );

        BigDecimal liabilities = sumByType(
                accounts,
                AccountType.LIABILITY
        );

        BigDecimal equity = sumByType(
                accounts,
                AccountType.EQUITY
        );

        BigDecimal netProfit =
                getProfitAndLoss(companyId).netProfit();

        BigDecimal totalLiabilitiesAndEquity =
                liabilities
                        .add(equity)
                        .add(netProfit);

        return new BalanceSheetResponse(
                assets,
                liabilities,
                equity,
                netProfit,
                totalLiabilitiesAndEquity
        );
    }

    private List<JournalEntryLine> getPostedLines(
            UUID companyId
    ) {
        return journalEntryLineRepository
                .findByCompanyIdAndJournalStatus(
                        companyId,
                        JournalEntryStatus.POSTED
                );
    }

    private AccountBalanceResponse toAccountBalance(
            Account account,
            BigDecimal debitTotal,
            BigDecimal creditTotal
    ) {
        BigDecimal balance;

        if (account.getAccountType() == AccountType.ASSET
                || account.getAccountType() == AccountType.EXPENSE) {

            balance = debitTotal.subtract(creditTotal);

        } else {

            balance = creditTotal.subtract(debitTotal);
        }

        return new AccountBalanceResponse(
                account.getId(),
                account.getAccountCode(),
                account.getName(),
                account.getAccountType(),
                debitTotal,
                creditTotal,
                balance
        );
    }

    private BigDecimal sumByType(
            List<AccountBalanceResponse> accounts,
            AccountType type
    ) {
        return accounts.stream()
                .filter(account ->
                        account.accountType() == type)
                .map(account -> account.balance() == null
                        ? BigDecimal.ZERO
                        : account.balance())
                .reduce(BigDecimal.ZERO, (runningTotal, value) ->
                        runningTotal.add(value == null ? BigDecimal.ZERO : value));
    }
}