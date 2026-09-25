package com.jewelvaulterp.dashboard;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.customer.repository.CustomerRepository;
import com.jewelvaulterp.dashboard.dto.DashboardSummaryResponse;
import com.jewelvaulterp.dashboard.service.DashboardService;
import com.jewelvaulterp.employee.entity.Employee;
import com.jewelvaulterp.employee.entity.EmploymentStatus;
import com.jewelvaulterp.employee.repository.EmployeeRepository;
import com.jewelvaulterp.expense.entity.Expense;
import com.jewelvaulterp.expense.entity.ExpenseCategory;
import com.jewelvaulterp.expense.entity.ExpensePaymentMethod;
import com.jewelvaulterp.expense.repository.ExpenseRepository;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.notification.entity.Notification;
import com.jewelvaulterp.notification.entity.NotificationType;
import com.jewelvaulterp.notification.repository.NotificationRepository;
import com.jewelvaulterp.payable.entity.Payable;
import com.jewelvaulterp.payable.repository.PayableRepository;
import com.jewelvaulterp.payroll.entity.Payroll;
import com.jewelvaulterp.payroll.entity.PayrollPaymentMethod;
import com.jewelvaulterp.payroll.repository.PayrollRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.purchase.entity.Purchase;
import com.jewelvaulterp.purchase.entity.PurchaseStatus;
import com.jewelvaulterp.purchase.repository.PurchaseRepository;
import com.jewelvaulterp.receivable.entity.Receivable;
import com.jewelvaulterp.receivable.repository.ReceivableRepository;
import com.jewelvaulterp.sale.entity.Sale;
import com.jewelvaulterp.sale.entity.SaleStatus;
import com.jewelvaulterp.sale.repository.SaleRepository;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.supplier.repository.SupplierRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ReceivableRepository receivableRepository;

    @Mock
    private PayableRepository payableRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PayrollRepository payrollRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private Company company;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        company = new Company(UUID.randomUUID(), "ACME", "Acme Corp", "US", "USD", LocalDateTime.now(), LocalDateTime.now());
        companyId = company.getId();
    }

    @Test
    void getSummary_shouldAggregateCompanyScopedMetrics() {
        User activeUser = new User(UUID.randomUUID(), company, "alice", "alice@acme.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
        User inactiveUser = new User(UUID.randomUUID(), company, "bob", "bob@acme.com", "hash", false, LocalDateTime.now(), LocalDateTime.now());
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(userRepository.findByCompanyId(companyId)).thenReturn(List.of(activeUser, inactiveUser));

        Sale sale = new Sale(UUID.randomUUID(), company, null, null, "S-100", LocalDateTime.now(), new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("0.00"), new BigDecimal("110.00"), SaleStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now());
        Sale cancelledSale = new Sale(UUID.randomUUID(), company, null, null, "S-200", LocalDateTime.now(), new BigDecimal("300.00"), new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("300.00"), SaleStatus.CANCELLED, LocalDateTime.now(), LocalDateTime.now());
        when(saleRepository.findByCompanyId(companyId)).thenReturn(List.of(sale, cancelledSale));

        Purchase purchase = new Purchase(UUID.randomUUID(), company, null, null, "P-100", LocalDateTime.now(), new BigDecimal("80.00"), new BigDecimal("8.00"), new BigDecimal("0.00"), new BigDecimal("88.00"), PurchaseStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());
        when(purchaseRepository.findByCompanyId(companyId)).thenReturn(List.of(purchase));

        ExpenseCategory category = new ExpenseCategory(company, "Operations", "General operations");
        Expense paidExpense = new Expense(company, null, category, "Utility bill", new BigDecimal("50.00"), LocalDateTime.now(), ExpensePaymentMethod.CASH, "EXP-1");
        paidExpense.approve();
        paidExpense.markPaid();
        Expense draftExpense = new Expense(company, null, category, "Draft expense", new BigDecimal("20.00"), LocalDateTime.now(), ExpensePaymentMethod.CASH, "EXP-2");
        when(expenseRepository.findByCompanyId(companyId)).thenReturn(List.of(paidExpense, draftExpense));

        Receivable receivable = new Receivable(company, null, null, new BigDecimal("60.00"), LocalDateTime.now().plusDays(5));
        receivable.recordPayment(new BigDecimal("20.00"));
        when(receivableRepository.findByCompanyId(companyId)).thenReturn(List.of(receivable));

        Payable payable = new Payable(company, null, null, new BigDecimal("70.00"), LocalDateTime.now().plusDays(10));
        payable.recordPayment(new BigDecimal("10.00"));
        when(payableRepository.findByCompanyId(companyId)).thenReturn(List.of(payable));

        Product product = new Product(UUID.randomUUID(), company, "SKU-01", "Ring", "Gold", "Gold", "22K", new BigDecimal("10.0"), new BigDecimal("9.0"), new BigDecimal("100.00"), true, LocalDateTime.now(), LocalDateTime.now());
        Product otherCompanyProduct = new Product(UUID.randomUUID(), new Company(UUID.randomUUID(), "BETA", "Beta", "US", "USD", LocalDateTime.now(), LocalDateTime.now()), "SKU-02", "Necklace", "Gold", "Gold", "22K", new BigDecimal("5.0"), new BigDecimal("4.8"), new BigDecimal("90.00"), true, LocalDateTime.now(), LocalDateTime.now());
        when(productRepository.findAll()).thenReturn(List.of(product, otherCompanyProduct));

        Inventory inventory = new Inventory(UUID.randomUUID(), null, product, new BigDecimal("5.0"), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());
        when(inventoryRepository.findAll()).thenReturn(List.of(inventory));

        Employee activeEmployee = new Employee(company, null, "E-001", "Alice", "Smith", "alice@acme.com", "123", "Addr", "Sales", "Manager", LocalDate.now());
        Employee inactiveEmployee = new Employee(company, null, "E-002", "Bob", "Johnson", "bob@acme.com", "456", "Addr", "Support", "Agent", LocalDate.now());
        inactiveEmployee.updateStatus(EmploymentStatus.INACTIVE);
        when(employeeRepository.findByCompanyId(companyId)).thenReturn(List.of(activeEmployee, inactiveEmployee));

        Customer customer = new Customer(UUID.randomUUID(), company, "Customer A", "C-001", "123", "a@acme.com", "Addr", "TAX-1", true, LocalDateTime.now(), LocalDateTime.now());
        Customer otherCompanyCustomer = new Customer(UUID.randomUUID(), new Company(UUID.randomUUID(), "BETA", "Beta", "US", "USD", LocalDateTime.now(), LocalDateTime.now()), "Customer B", "C-002", "456", "b@beta.com", "Addr", "TAX-2", true, LocalDateTime.now(), LocalDateTime.now());
        when(customerRepository.findAll()).thenReturn(List.of(customer, otherCompanyCustomer));

        Supplier supplier = new Supplier(UUID.randomUUID(), company, "Supplier A", "S-001", "Contact", "789", "s@acme.com", "Addr", "TAX-3", true, LocalDateTime.now(), LocalDateTime.now());
        Supplier otherCompanySupplier = new Supplier(UUID.randomUUID(), new Company(UUID.randomUUID(), "BETA", "Beta", "US", "USD", LocalDateTime.now(), LocalDateTime.now()), "Supplier B", "S-002", "Contact", "000", "s@beta.com", "Addr", "TAX-4", true, LocalDateTime.now(), LocalDateTime.now());
        when(supplierRepository.findAll()).thenReturn(List.of(supplier, otherCompanySupplier));

        when(notificationRepository.findByCompanyIdAndReadFalseOrderByCreatedAtDesc(companyId)).thenReturn(List.of(
                new Notification(UUID.randomUUID(), company, activeUser, NotificationType.SYSTEM, "Alert 1", "Message 1", null, null, false, LocalDateTime.now(), null),
                new Notification(UUID.randomUUID(), company, activeUser, NotificationType.SYSTEM, "Alert 2", "Message 2", null, null, false, LocalDateTime.now(), null)
        ));

        Payroll payroll = new Payroll(company, activeUser, LocalDateTime.now().getYear(), LocalDateTime.now().getMonthValue(), new BigDecimal("4000.00"), new BigDecimal("200.00"), new BigDecimal("150.00"));
        payroll.markPaid(LocalDate.now(), PayrollPaymentMethod.BANK_TRANSFER, "PAY-001");
        when(payrollRepository.findByCompanyId(companyId)).thenReturn(List.of(payroll));

        DashboardSummaryResponse response = dashboardService.getSummary(companyId);

        assertEquals(companyId, response.companyId());
        assertEquals(new BigDecimal("110.00"), response.totalSales());
        assertEquals(new BigDecimal("88.00"), response.totalPurchases());
        assertEquals(new BigDecimal("50.00"), response.totalExpenses());
        assertEquals(new BigDecimal("40.00"), response.outstandingReceivables());
        assertEquals(new BigDecimal("60.00"), response.outstandingPayables());
        assertEquals(new BigDecimal("500.00"), response.inventoryValue());
        assertEquals(1, response.activeUsers());
        assertEquals(1, response.totalCustomers());
        assertEquals(1, response.totalSuppliers());
        assertEquals(1, response.totalProducts());
        assertEquals(1, response.activeEmployees());
        assertEquals(new BigDecimal("110.00"), response.salesToday());
        assertEquals(new BigDecimal("110.00"), response.salesThisMonth());
        assertEquals(new BigDecimal("50.00"), response.expensesToday());
        assertEquals(new BigDecimal("50.00"), response.expensesThisMonth());
        assertEquals(new BigDecimal("4050.00"), response.payrollThisMonth());
        assertEquals(2L, response.unreadNotifications());
    }

    @Test
    void getSummary_shouldRejectUnknownCompany() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getSummary(companyId));

        assertTrue(exception.getMessage().contains("Company not found"));
    }
}
