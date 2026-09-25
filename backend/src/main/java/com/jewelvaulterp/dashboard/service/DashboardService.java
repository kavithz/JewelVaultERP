package com.jewelvaulterp.dashboard.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.customer.repository.CustomerRepository;
import com.jewelvaulterp.dashboard.dto.DashboardSummaryResponse;
import com.jewelvaulterp.employee.entity.Employee;
import com.jewelvaulterp.employee.entity.EmploymentStatus;
import com.jewelvaulterp.employee.repository.EmployeeRepository;
import com.jewelvaulterp.expense.entity.Expense;
import com.jewelvaulterp.expense.entity.ExpenseStatus;
import com.jewelvaulterp.expense.repository.ExpenseRepository;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.notification.entity.Notification;
import com.jewelvaulterp.notification.repository.NotificationRepository;
import com.jewelvaulterp.payable.entity.Payable;
import com.jewelvaulterp.payable.entity.PayableStatus;
import com.jewelvaulterp.payable.repository.PayableRepository;
import com.jewelvaulterp.payroll.entity.Payroll;
import com.jewelvaulterp.payroll.entity.PayrollStatus;
import com.jewelvaulterp.payroll.repository.PayrollRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.purchase.entity.Purchase;
import com.jewelvaulterp.purchase.entity.PurchaseStatus;
import com.jewelvaulterp.purchase.repository.PurchaseRepository;
import com.jewelvaulterp.receivable.entity.Receivable;
import com.jewelvaulterp.receivable.entity.ReceivableStatus;
import com.jewelvaulterp.receivable.repository.ReceivableRepository;
import com.jewelvaulterp.sale.entity.Sale;
import com.jewelvaulterp.sale.entity.SaleStatus;
import com.jewelvaulterp.sale.repository.SaleRepository;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.supplier.repository.SupplierRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional
public class DashboardService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final ExpenseRepository expenseRepository;
    private final ReceivableRepository receivableRepository;
    private final PayableRepository payableRepository;
    private final InventoryRepository inventoryRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;
    private final PayrollRepository payrollRepository;

    public DashboardService(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            SaleRepository saleRepository,
            PurchaseRepository purchaseRepository,
            ExpenseRepository expenseRepository,
            ReceivableRepository receivableRepository,
            PayableRepository payableRepository,
            InventoryRepository inventoryRepository,
            EmployeeRepository employeeRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            NotificationRepository notificationRepository,
            PayrollRepository payrollRepository
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.expenseRepository = expenseRepository;
        this.receivableRepository = receivableRepository;
        this.payableRepository = payableRepository;
        this.inventoryRepository = inventoryRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.notificationRepository = notificationRepository;
        this.payrollRepository = payrollRepository;
    }

    public DashboardSummaryResponse getSummary() {
        return companyRepository.findAll().stream()
                .findFirst()
                .map(company -> getSummary(company.getId()))
                .orElseGet(() -> new DashboardSummaryResponse(
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0L,
                        0L,
                        0L,
                        0L,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0L
                ));
    }

    public DashboardSummaryResponse getSummary(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        List<User> users = userRepository.findByCompanyId(companyId);
        List<Sale> sales = saleRepository.findByCompanyId(companyId).stream()
                .filter(this::isActiveSale)
                .toList();
        List<Purchase> purchases = purchaseRepository.findByCompanyId(companyId).stream()
                .filter(this::isActivePurchase)
                .toList();
        List<Expense> expenses = expenseRepository.findByCompanyId(companyId).stream()
                .filter(this::isActiveExpense)
                .toList();
        List<Receivable> receivables = receivableRepository.findByCompanyId(companyId).stream()
                .filter(this::isActiveReceivable)
                .toList();
        List<Payable> payables = payableRepository.findByCompanyId(companyId).stream()
                .filter(this::isActivePayable)
                .toList();
        List<Inventory> inventories = inventoryRepository.findAll().stream()
                .filter(item -> item.getProduct() != null)
                .filter(item -> companyId.equals(item.getProduct().getCompany().getId()))
                .toList();
        List<Employee> employees = employeeRepository.findByCompanyId(companyId);
        List<Customer> customers = customerRepository.findAll().stream()
                .filter(item -> item.getCompany() != null && companyId.equals(item.getCompany().getId()))
                .toList();
        List<Supplier> suppliers = supplierRepository.findAll().stream()
                .filter(item -> item.getCompany() != null && companyId.equals(item.getCompany().getId()))
                .toList();
        List<Product> products = productRepository.findAll().stream()
                .filter(item -> item.getCompany() != null && companyId.equals(item.getCompany().getId()))
                .toList();
        List<Notification> unreadNotifications = notificationRepository.findByCompanyIdAndReadFalseOrderByCreatedAtDesc(companyId);
        List<Payroll> payrolls = payrollRepository.findByCompanyId(companyId).stream()
                .filter(item -> item.getStatus() != PayrollStatus.CANCELLED)
                .toList();

        BigDecimal totalSales = sum(sales.stream().map(Sale::getTotalAmount));
        BigDecimal totalPurchases = sum(purchases.stream().map(Purchase::getTotalAmount));
        BigDecimal totalExpenses = sum(expenses.stream().map(Expense::getAmount));
        BigDecimal outstandingReceivables = sum(receivables.stream().map(Receivable::getOutstandingAmount));
        BigDecimal outstandingPayables = sum(payables.stream().map(Payable::getOutstandingAmount));
        BigDecimal inventoryValue = sum(inventories.stream()
                .map(item -> item.getProduct() != null
                        ? item.getQuantity().multiply(productValue(item.getProduct())).setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)));

        long activeUsers = users.stream().filter(User::isActive).count();
        long totalCustomers = customers.size();
        long totalSuppliers = suppliers.size();
        long totalProducts = products.size();
        long activeEmployees = employees.stream()
                .filter(employee -> employee.getEmploymentStatus() == EmploymentStatus.ACTIVE)
                .count();

        LocalDate today = LocalDate.now();
        BigDecimal salesToday = sum(sales.stream()
                .filter(sale -> sale.getSaleDate() != null && sale.getSaleDate().toLocalDate().equals(today))
                .map(Sale::getTotalAmount));
        BigDecimal salesThisMonth = sum(sales.stream()
                .filter(sale -> isInCurrentMonth(sale.getSaleDate()))
                .map(Sale::getTotalAmount));
        BigDecimal expensesToday = sum(expenses.stream()
                .filter(expense -> expense.getExpenseDate() != null && expense.getExpenseDate().toLocalDate().equals(today))
                .map(Expense::getAmount));
        BigDecimal expensesThisMonth = sum(expenses.stream()
                .filter(expense -> isInCurrentMonth(expense.getExpenseDate()))
                .map(Expense::getAmount));
        BigDecimal payrollThisMonth = sum(payrolls.stream()
                .filter(payroll -> isInCurrentMonth(payroll.getPaymentDate() != null ? payroll.getPaymentDate().atStartOfDay() : payroll.getCreatedAt()))
                .map(Payroll::getNetSalary));

        return new DashboardSummaryResponse(
                company.getId(),
                totalSales,
                totalPurchases,
                totalExpenses,
                outstandingReceivables,
                outstandingPayables,
                inventoryValue,
                activeUsers,
                totalCustomers,
                totalSuppliers,
                totalProducts,
                activeEmployees,
                salesToday,
                salesThisMonth,
                expensesToday,
                expensesThisMonth,
                payrollThisMonth,
                unreadNotifications.size()
        );
    }

    private boolean isActiveSale(Sale sale) {
        return sale != null && sale.getStatus() != SaleStatus.CANCELLED;
    }

    private boolean isActivePurchase(Purchase purchase) {
        return purchase != null && purchase.getStatus() != PurchaseStatus.CANCELLED;
    }

    private boolean isActiveExpense(Expense expense) {
        return expense != null && expense.getStatus() != ExpenseStatus.DRAFT && expense.getStatus() != ExpenseStatus.CANCELLED;
    }

    private boolean isActiveReceivable(Receivable receivable) {
        return receivable != null && receivable.getStatus() != ReceivableStatus.CANCELLED;
    }

    private boolean isActivePayable(Payable payable) {
        return payable != null && payable.getStatus() != PayableStatus.CANCELLED;
    }

    private boolean isInCurrentMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        return dateTime.toLocalDate().getYear() == now.getYear()
                && dateTime.toLocalDate().getMonthValue() == now.getMonthValue();
    }

    private BigDecimal productValue(Product product) {
        if (product == null) {
            return BigDecimal.ZERO;
        }
        return product.getMakingCharge() == null ? BigDecimal.ZERO : product.getMakingCharge();
    }

    private BigDecimal sum(Stream<BigDecimal> values) {
        BigDecimal total = values.filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}