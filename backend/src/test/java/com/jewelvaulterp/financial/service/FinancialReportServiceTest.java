package com.jewelvaulterp.financial.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.expense.entity.Expense;
import com.jewelvaulterp.expense.entity.ExpensePaymentMethod;
import com.jewelvaulterp.expense.entity.ExpenseStatus;
import com.jewelvaulterp.expense.repository.ExpenseRepository;
import com.jewelvaulterp.financial.dto.CashFlowSummaryResponse;
import com.jewelvaulterp.financial.dto.ExpenseSummaryResponse;
import com.jewelvaulterp.payment.entity.Payment;
import com.jewelvaulterp.payment.entity.PaymentMethod;
import com.jewelvaulterp.payment.entity.PaymentStatus;
import com.jewelvaulterp.payment.entity.PaymentType;
import com.jewelvaulterp.payment.repository.PaymentRepository;
import com.jewelvaulterp.payable.repository.PayableRepository;
import com.jewelvaulterp.purchase.repository.PurchaseRepository;
import com.jewelvaulterp.receivable.repository.ReceivableRepository;
import com.jewelvaulterp.sale.repository.SaleRepository;
import com.jewelvaulterp.tax.repository.TaxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancialReportServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private PayableRepository payableRepository;

    @Mock
    private TaxRepository taxRepository;

    private FinancialReportService service;
    private Company company;

    @BeforeEach
    void setUp() {
        service = new FinancialReportService(
                companyRepository,
                saleRepository,
                purchaseRepository,
                expenseRepository,
                paymentRepository,
                receivableRepository,
                payableRepository,
                taxRepository
        );
        company = new Company(
                UUID.randomUUID(),
                "JewelVault",
                "JewelVault ERP",
                "US",
                "USD",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(companyRepository.findById(company.getId()))
                .thenReturn(Optional.of(company));
    }

    @Test
    void emptyCompanyReturnsZeroValues() {
        when(saleRepository.findByCompanyId(company.getId())).thenReturn(List.of());
        when(purchaseRepository.findByCompanyId(company.getId())).thenReturn(List.of());
        when(expenseRepository.findByCompanyId(company.getId())).thenReturn(List.of());
        when(paymentRepository.findByCompanyId(company.getId())).thenReturn(List.of());
        when(receivableRepository.findByCompanyId(company.getId())).thenReturn(List.of());
        when(payableRepository.findByCompanyId(company.getId())).thenReturn(List.of());

        var sales = service.getSalesSummary(company.getId());
        var purchases = service.getPurchaseSummary(company.getId());
        var expenses = service.getExpenseSummary(company.getId());
        var cashFlow = service.getCashFlowSummary(company.getId());
        var summary = service.getFinancialSummary(company.getId());

        assertEquals(BigDecimal.ZERO, sales.totalSales());
        assertEquals(BigDecimal.ZERO, purchases.totalPurchases());
        assertEquals(BigDecimal.ZERO, expenses.totalExpenses());
        assertEquals(BigDecimal.ZERO, cashFlow.netCashFlow());
        assertEquals(BigDecimal.ZERO, summary.netCashFlow());
    }

    @Test
    void cashFlowUsesCompletedPaymentsOnly() {
        when(paymentRepository.findByCompanyId(company.getId())).thenReturn(List.of(
                payment(company, "PAY-1", PaymentType.CUSTOMER_PAYMENT, PaymentStatus.COMPLETED, new BigDecimal("250.00")),
                payment(company, "PAY-2", PaymentType.SUPPLIER_PAYMENT, PaymentStatus.COMPLETED, new BigDecimal("100.00")),
                payment(company, "PAY-3", PaymentType.CUSTOMER_PAYMENT, PaymentStatus.CANCELLED, new BigDecimal("999.00")),
                payment(company, "PAY-4", PaymentType.SUPPLIER_PAYMENT, PaymentStatus.PENDING, new BigDecimal("333.00"))
        ));

        CashFlowSummaryResponse result = service.getCashFlowSummary(company.getId());

        assertEquals(new BigDecimal("250.00"), result.cashInflow());
        assertEquals(new BigDecimal("100.00"), result.cashOutflow());
        assertEquals(new BigDecimal("150.00"), result.netCashFlow());
    }

    @Test
    void expenseSummaryExcludesCancelledAndDraft() {
        when(expenseRepository.findByCompanyId(company.getId())).thenReturn(List.of(
                approvedExpense(company, new BigDecimal("200.00")),
                paidExpense(company, new BigDecimal("50.00")),
                cancelledExpense(company, new BigDecimal("999.00")),
                draftExpense(company, new BigDecimal("111.00"))
        ));

        ExpenseSummaryResponse result = service.getExpenseSummary(company.getId());

        assertEquals(new BigDecimal("250.00"), result.totalExpenses());
        assertEquals(new BigDecimal("50.00"), result.totalPaid());
        assertEquals(new BigDecimal("200.00"), result.totalOutstanding());
        assertEquals(2L, result.expenseCount());
    }

    private Expense approvedExpense(Company company, BigDecimal amount) {
        Expense expense = new Expense(
                company,
                null,
                null,
                "Approved expense",
                amount,
                LocalDateTime.now(),
                ExpensePaymentMethod.CASH,
                "REF-APPROVED"
        );
        expense.approve();
        return expense;
    }

    private Expense paidExpense(Company company, BigDecimal amount) {
        Expense expense = new Expense(
                company,
                null,
                null,
                "Paid expense",
                amount,
                LocalDateTime.now(),
                ExpensePaymentMethod.CASH,
                "REF-PAID"
        );
        expense.approve();
        expense.markPaid();
        return expense;
    }

    private Expense cancelledExpense(Company company, BigDecimal amount) {
        Expense expense = new Expense(
                company,
                null,
                null,
                "Cancelled expense",
                amount,
                LocalDateTime.now(),
                ExpensePaymentMethod.CASH,
                "REF-CANCELLED"
        );
        expense.cancel();
        return expense;
    }

    private Expense draftExpense(Company company, BigDecimal amount) {
        return new Expense(
                company,
                null,
                null,
                "Draft expense",
                amount,
                LocalDateTime.now(),
                ExpensePaymentMethod.CASH,
                "REF-DRAFT"
        );
    }

    private Payment payment(
            Company company,
            String paymentNumber,
            PaymentType paymentType,
            PaymentStatus status,
            BigDecimal amount
    ) {
        return new Payment(
                UUID.randomUUID(),
                company,
                paymentNumber,
                LocalDateTime.now(),
                paymentType,
                PaymentMethod.CASH,
                amount,
                "REF-" + paymentNumber,
                "notes",
                status,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
