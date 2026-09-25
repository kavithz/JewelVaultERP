package com.jewelvaulterp.financial.service;

import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.expense.entity.Expense;
import com.jewelvaulterp.expense.entity.ExpenseStatus;
import com.jewelvaulterp.expense.repository.ExpenseRepository;
import com.jewelvaulterp.financial.dto.CashFlowSummaryResponse;
import com.jewelvaulterp.financial.dto.ExpenseSummaryResponse;
import com.jewelvaulterp.financial.dto.FinancialSummaryResponse;
import com.jewelvaulterp.financial.dto.PayablesSummaryResponse;
import com.jewelvaulterp.financial.dto.PurchaseSummaryResponse;
import com.jewelvaulterp.financial.dto.ReceivablesSummaryResponse;
import com.jewelvaulterp.financial.dto.SalesSummaryResponse;
import com.jewelvaulterp.financial.dto.TaxSummaryResponse;
import com.jewelvaulterp.payment.entity.Payment;
import com.jewelvaulterp.payment.entity.PaymentStatus;
import com.jewelvaulterp.payment.entity.PaymentType;
import com.jewelvaulterp.payment.repository.PaymentRepository;
import com.jewelvaulterp.payable.entity.Payable;
import com.jewelvaulterp.payable.entity.PayableStatus;
import com.jewelvaulterp.payable.repository.PayableRepository;
import com.jewelvaulterp.purchase.entity.Purchase;
import com.jewelvaulterp.purchase.entity.PurchaseStatus;
import com.jewelvaulterp.purchase.repository.PurchaseRepository;
import com.jewelvaulterp.receivable.entity.Receivable;
import com.jewelvaulterp.receivable.entity.ReceivableStatus;
import com.jewelvaulterp.receivable.repository.ReceivableRepository;
import com.jewelvaulterp.sale.entity.Sale;
import com.jewelvaulterp.sale.entity.SaleStatus;
import com.jewelvaulterp.sale.repository.SaleRepository;
import com.jewelvaulterp.tax.repository.TaxRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FinancialReportService {

    private final CompanyRepository companyRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final ReceivableRepository receivableRepository;
    private final PayableRepository payableRepository;
    private final TaxRepository taxRepository;

    public FinancialReportService(
            CompanyRepository companyRepository,
            SaleRepository saleRepository,
            PurchaseRepository purchaseRepository,
            ExpenseRepository expenseRepository,
            PaymentRepository paymentRepository,
            ReceivableRepository receivableRepository,
            PayableRepository payableRepository,
            TaxRepository taxRepository
    ) {
        this.companyRepository = companyRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.receivableRepository = receivableRepository;
        this.payableRepository = payableRepository;
        this.taxRepository = taxRepository;
    }

    public SalesSummaryResponse getSalesSummary(UUID companyId) {
        ensureCompanyExists(companyId);

        List<Sale> sales = saleRepository.findByCompanyId(companyId).stream()
                .filter(this::isActiveSale)
                .toList();

        ReceivablesSummaryResponse receivables = getReceivablesSummary(companyId);

        return new SalesSummaryResponse(
                sumBigDecimals(sales.stream().map(Sale::getTotalAmount)),
                receivables.totalPaid(),
                receivables.totalOutstanding(),
                sales.size()
        );
    }

    public PurchaseSummaryResponse getPurchaseSummary(UUID companyId) {
        ensureCompanyExists(companyId);

        List<Purchase> purchases = purchaseRepository.findByCompanyId(companyId).stream()
                .filter(this::isActivePurchase)
                .toList();

        PayablesSummaryResponse payables = getPayablesSummary(companyId);

        return new PurchaseSummaryResponse(
                sumBigDecimals(purchases.stream().map(Purchase::getTotalAmount)),
                payables.totalPaid(),
                payables.totalOutstanding(),
                purchases.size()
        );
    }

    public ExpenseSummaryResponse getExpenseSummary(UUID companyId) {
        ensureCompanyExists(companyId);

        List<Expense> expenses = expenseRepository.findByCompanyId(companyId).stream()
                .filter(this::isActiveExpense)
                .toList();

        BigDecimal totalExpenses = sumBigDecimals(
                expenses.stream().map(Expense::getAmount)
        );
        BigDecimal totalPaid = sumBigDecimals(
                expenses.stream()
                        .filter(expense -> expense.getStatus() == ExpenseStatus.PAID)
                        .map(Expense::getAmount)
        );
        BigDecimal totalOutstanding = sumBigDecimals(
                expenses.stream()
                        .filter(expense -> expense.getStatus() == ExpenseStatus.APPROVED)
                        .map(Expense::getAmount)
        );

        return new ExpenseSummaryResponse(
                totalExpenses,
                totalPaid,
                totalOutstanding,
                expenses.size()
        );
    }

    public CashFlowSummaryResponse getCashFlowSummary(UUID companyId) {
        ensureCompanyExists(companyId);

        List<Payment> payments = paymentRepository.findByCompanyId(companyId).stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .toList();

        BigDecimal cashInflow = sumBigDecimals(
                payments.stream()
                        .filter(payment -> payment.getPaymentType() == PaymentType.CUSTOMER_PAYMENT)
                        .map(Payment::getAmount)
        );
        BigDecimal cashOutflow = sumBigDecimals(
                payments.stream()
                        .filter(payment -> payment.getPaymentType() == PaymentType.SUPPLIER_PAYMENT)
                        .map(Payment::getAmount)
        );

        return new CashFlowSummaryResponse(
                cashInflow,
                cashOutflow,
                cashInflow.subtract(cashOutflow)
        );
    }

    public ReceivablesSummaryResponse getReceivablesSummary(UUID companyId) {
        ensureCompanyExists(companyId);

        List<Receivable> receivables = receivableRepository.findByCompanyId(companyId).stream()
                .filter(receivable -> receivable.getStatus() != ReceivableStatus.CANCELLED)
                .toList();

        BigDecimal totalReceivables = sumBigDecimals(
                receivables.stream().map(Receivable::getAmount)
        );
        BigDecimal totalPaid = sumBigDecimals(
                receivables.stream().map(Receivable::getPaidAmount)
        );
        BigDecimal totalOutstanding = sumBigDecimals(
                receivables.stream().map(Receivable::getOutstandingAmount)
        );
        BigDecimal overdueAmount = sumBigDecimals(
                receivables.stream()
                        .filter(receivable -> receivable.getStatus() == ReceivableStatus.OVERDUE)
                        .map(Receivable::getOutstandingAmount)
        );

        return new ReceivablesSummaryResponse(
                totalReceivables,
                totalPaid,
                totalOutstanding,
                overdueAmount,
                receivables.size()
        );
    }

    public PayablesSummaryResponse getPayablesSummary(UUID companyId) {
        ensureCompanyExists(companyId);

        List<Payable> payables = payableRepository.findByCompanyId(companyId).stream()
                .filter(payable -> payable.getStatus() != PayableStatus.CANCELLED)
                .toList();

        BigDecimal totalPayables = sumBigDecimals(
                payables.stream().map(Payable::getAmount)
        );
        BigDecimal totalPaid = sumBigDecimals(
                payables.stream().map(Payable::getPaidAmount)
        );
        BigDecimal totalOutstanding = sumBigDecimals(
                payables.stream().map(Payable::getOutstandingAmount)
        );
        BigDecimal overdueAmount = sumBigDecimals(
                payables.stream()
                        .filter(payable -> payable.getStatus() == PayableStatus.OVERDUE)
                        .map(Payable::getOutstandingAmount)
        );

        return new PayablesSummaryResponse(
                totalPayables,
                totalPaid,
                totalOutstanding,
                overdueAmount,
                payables.size()
        );
    }

    public TaxSummaryResponse getTaxSummary(UUID companyId) {
        ensureCompanyExists(companyId);

        List<Sale> sales = saleRepository.findByCompanyId(companyId).stream()
                .filter(this::isActiveSale)
                .toList();
        List<Purchase> purchases = purchaseRepository.findByCompanyId(companyId).stream()
                .filter(this::isActivePurchase)
                .toList();

        BigDecimal salesTax = sumBigDecimals(
                sales.stream().map(Sale::getTaxAmount)
        );
        BigDecimal purchaseTax = sumBigDecimals(
                purchases.stream().map(Purchase::getTaxAmount)
        );

        return new TaxSummaryResponse(
                salesTax.add(purchaseTax),
                salesTax,
                purchaseTax,
                sales.size() + purchases.size()
        );
    }

    public FinancialSummaryResponse getFinancialSummary(UUID companyId) {
        SalesSummaryResponse salesSummary = getSalesSummary(companyId);
        PurchaseSummaryResponse purchaseSummary = getPurchaseSummary(companyId);
        ExpenseSummaryResponse expenseSummary = getExpenseSummary(companyId);
        ReceivablesSummaryResponse receivables = getReceivablesSummary(companyId);
        PayablesSummaryResponse payables = getPayablesSummary(companyId);
        TaxSummaryResponse taxSummary = getTaxSummary(companyId);
        CashFlowSummaryResponse cashFlow = getCashFlowSummary(companyId);

        return new FinancialSummaryResponse(
                salesSummary.totalSales(),
                purchaseSummary.totalPurchases(),
                expenseSummary.totalExpenses(),
                receivables.totalOutstanding(),
                payables.totalOutstanding(),
                taxSummary.totalTax(),
                cashFlow.netCashFlow()
        );
    }

    private void ensureCompanyExists(UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    private boolean isActiveSale(Sale sale) {
        return sale != null && sale.getStatus() != SaleStatus.CANCELLED;
    }

    private boolean isActivePurchase(Purchase purchase) {
        return purchase != null && purchase.getStatus() != PurchaseStatus.CANCELLED;
    }

    private boolean isActiveExpense(Expense expense) {
        return expense != null
                && expense.getStatus() != ExpenseStatus.DRAFT
                && expense.getStatus() != ExpenseStatus.CANCELLED;
    }

    private BigDecimal sumBigDecimals(java.util.stream.Stream<BigDecimal> values) {
        return values
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
