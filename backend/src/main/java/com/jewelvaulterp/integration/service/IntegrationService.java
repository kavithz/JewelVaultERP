package com.jewelvaulterp.integration.service;

import com.jewelvaulterp.accounting.entity.Account;
import com.jewelvaulterp.accounting.repository.AccountRepository;
import com.jewelvaulterp.audit.entity.AuditLog;
import com.jewelvaulterp.audit.repository.AuditLogRepository;
import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.branch.repository.BranchRepository;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.customer.repository.CustomerRepository;
import com.jewelvaulterp.employee.entity.Employee;
import com.jewelvaulterp.employee.repository.EmployeeRepository;
import com.jewelvaulterp.integration.dto.*;
import com.jewelvaulterp.integration.exception.CompanyNotFoundException;
import com.jewelvaulterp.integration.exception.InvalidIntegrationRequestException;
import com.jewelvaulterp.invoice.entity.Invoice;
import com.jewelvaulterp.invoice.repository.InvoiceRepository;
import com.jewelvaulterp.payroll.entity.Payroll;
import com.jewelvaulterp.payroll.repository.PayrollRepository;
import com.jewelvaulterp.payment.entity.Payment;
import com.jewelvaulterp.payment.repository.PaymentRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.purchase.entity.Purchase;
import com.jewelvaulterp.purchase.repository.PurchaseRepository;
import com.jewelvaulterp.sale.entity.Sale;
import com.jewelvaulterp.sale.repository.SaleRepository;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.supplier.repository.SupplierRepository;
import com.jewelvaulterp.tax.entity.Tax;
import com.jewelvaulterp.tax.repository.TaxRepository;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Service
public class IntegrationService {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 500;

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final TaxRepository taxRepository;
    private final BranchRepository branchRepository;
    private final WarehouseRepository warehouseRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PayrollRepository payrollRepository;
    private final AuditLogRepository auditLogRepository;

    public IntegrationService(
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            AccountRepository accountRepository,
            TaxRepository taxRepository,
            BranchRepository branchRepository,
            WarehouseRepository warehouseRepository,
            SaleRepository saleRepository,
            PurchaseRepository purchaseRepository,
            PaymentRepository paymentRepository,
            InvoiceRepository invoiceRepository,
            PayrollRepository payrollRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.taxRepository = taxRepository;
        this.branchRepository = branchRepository;
        this.warehouseRepository = warehouseRepository;
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.payrollRepository = payrollRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public IntegrationHealthResponse getHealth() {
        return new IntegrationHealthResponse("JewelVaultERP", "UP", LocalDateTime.now());
    }

    public IntegrationPageResponse<IntegrationProductResponse> getProducts(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Product> result = updatedSince == null
                ? productRepository.findByCompanyId(companyId, pageable)
                : productRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toProductResponse);
    }

    public IntegrationPageResponse<IntegrationCustomerResponse> getCustomers(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Customer> result = updatedSince == null
                ? customerRepository.findByCompanyId(companyId, pageable)
                : customerRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toCustomerResponse);
    }

    public IntegrationPageResponse<IntegrationSupplierResponse> getSuppliers(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Supplier> result = updatedSince == null
                ? supplierRepository.findByCompanyId(companyId, pageable)
                : supplierRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toSupplierResponse);
    }

    public IntegrationPageResponse<IntegrationEmployeeResponse> getEmployees(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Employee> result = updatedSince == null
                ? employeeRepository.findByCompanyId(companyId, pageable)
                : employeeRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toEmployeeResponse);
    }

    public IntegrationPageResponse<IntegrationAccountResponse> getAccounts(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Account> result = updatedSince == null
                ? accountRepository.findByCompanyId(companyId, pageable)
                : accountRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toAccountResponse);
    }

    public IntegrationPageResponse<IntegrationTaxResponse> getTaxes(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Tax> result = updatedSince == null
                ? taxRepository.findByCompanyId(companyId, pageable)
                : taxRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toTaxResponse);
    }

    public IntegrationPageResponse<IntegrationWarehouseResponse> getWarehouses(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Warehouse> result = updatedSince == null
                ? warehouseRepository.findByBranchCompanyId(companyId, pageable)
                : warehouseRepository.findByBranchCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toWarehouseResponse);
    }

    public IntegrationPageResponse<IntegrationBranchResponse> getBranches(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Branch> result = updatedSince == null
                ? branchRepository.findByCompanyId(companyId, pageable)
                : branchRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toBranchResponse);
    }

    public IntegrationPageResponse<IntegrationSaleResponse> getSales(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Sale> result = updatedSince == null
                ? saleRepository.findByCompanyId(companyId, pageable)
                : saleRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toSaleResponse);
    }

    public IntegrationPageResponse<IntegrationPurchaseResponse> getPurchases(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Purchase> result = updatedSince == null
                ? purchaseRepository.findByCompanyId(companyId, pageable)
                : purchaseRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toPurchaseResponse);
    }

    public IntegrationPageResponse<IntegrationPaymentResponse> getPayments(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Payment> result = updatedSince == null
                ? paymentRepository.findByCompanyId(companyId, pageable)
                : paymentRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toPaymentResponse);
    }

    public IntegrationPageResponse<IntegrationInvoiceResponse> getInvoices(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Invoice> result = updatedSince == null
                ? invoiceRepository.findByCompanyId(companyId, pageable)
                : invoiceRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toInvoiceResponse);
    }

    public IntegrationPageResponse<IntegrationPayrollResponse> getPayroll(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<Payroll> result = updatedSince == null
                ? payrollRepository.findByCompanyId(companyId, pageable)
                : payrollRepository.findByCompanyIdAndUpdatedAtAfter(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toPayrollResponse);
    }

    public IntegrationPageResponse<IntegrationAuditLogResponse> getAuditLogs(
            UUID companyId,
            LocalDateTime updatedSince,
            int page,
            int size
    ) {
        validateCompanyExists(companyId);
        Pageable pageable = buildPageable(page, size);
        Page<AuditLog> result = updatedSince == null
                ? auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId, pageable)
                : auditLogRepository.findByCompanyIdAndCreatedAtAfterOrderByCreatedAtDesc(companyId, updatedSince, pageable);
        return toPageResponse(result, this::toAuditLogResponse);
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 0) {
            throw new InvalidIntegrationRequestException("Page index must be greater than or equal to 0.");
        }
        if (size < 1) {
            throw new InvalidIntegrationRequestException("Page size must be greater than 0.");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new InvalidIntegrationRequestException("Maximum page size is 500.");
        }
        return PageRequest.of(page, size == 0 ? DEFAULT_PAGE_SIZE : size);
    }

    private void validateCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException(companyId);
        }
    }

    private <T, R> IntegrationPageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        return new IntegrationPageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private IntegrationProductResponse toProductResponse(Product product) {
        return new IntegrationProductResponse(
                product.getId(),
                product.getCompany().getId(),
                product.getSku(),
                product.getName(),
                product.getJewelleryType(),
                product.getMetalType(),
                product.getPurity(),
                product.getGrossWeight(),
                product.getNetWeight(),
                product.getMakingCharge(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private IntegrationCustomerResponse toCustomerResponse(Customer customer) {
        return new IntegrationCustomerResponse(
                customer.getId(),
                customer.getCompany().getId(),
                customer.getName(),
                customer.getCode(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getTaxNumber(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    private IntegrationSupplierResponse toSupplierResponse(Supplier supplier) {
        return new IntegrationSupplierResponse(
                supplier.getId(),
                supplier.getCompany().getId(),
                supplier.getName(),
                supplier.getCode(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getTaxNumber(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }

    private IntegrationEmployeeResponse toEmployeeResponse(Employee employee) {
        return new IntegrationEmployeeResponse(
                employee.getId(),
                employee.getCompany().getId(),
                employee.getBranch() == null ? null : employee.getBranch().getId(),
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getAddress(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getEmploymentStatus(),
                employee.getJoiningDate(),
                employee.getExitDate(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }

    private IntegrationAccountResponse toAccountResponse(Account account) {
        return new IntegrationAccountResponse(
                account.getId(),
                account.getCompany().getId(),
                account.getAccountCode(),
                account.getName(),
                account.getAccountType(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private IntegrationTaxResponse toTaxResponse(Tax tax) {
        return new IntegrationTaxResponse(
                tax.getId(),
                tax.getCompany().getId(),
                tax.getName(),
                tax.getCode(),
                tax.getTaxType(),
                tax.getRate(),
                tax.getDescription(),
                tax.isActive(),
                tax.getCreatedAt(),
                tax.getUpdatedAt()
        );
    }

    private IntegrationBranchResponse toBranchResponse(Branch branch) {
        return new IntegrationBranchResponse(
                branch.getId(),
                branch.getCompany().getId(),
                branch.getName(),
                branch.getCode(),
                branch.getAddress(),
                branch.getCity(),
                branch.getCountryCode(),
                branch.getPhone(),
                branch.getEmail(),
                branch.isActive(),
                branch.getCreatedAt(),
                branch.getUpdatedAt()
        );
    }

    private IntegrationWarehouseResponse toWarehouseResponse(Warehouse warehouse) {
        return new IntegrationWarehouseResponse(
                warehouse.getId(),
                warehouse.getBranch().getCompany().getId(),
                warehouse.getBranch().getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getAddress(),
                warehouse.getDescription(),
                warehouse.isActive(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }

    private IntegrationSaleResponse toSaleResponse(Sale sale) {
        return new IntegrationSaleResponse(
                sale.getId(),
                sale.getCompany().getId(),
                sale.getCustomer().getId(),
                sale.getWarehouse().getId(),
                sale.getSaleNumber(),
                sale.getSaleDate(),
                sale.getSubtotal(),
                sale.getTaxAmount(),
                sale.getDiscountAmount(),
                sale.getTotalAmount(),
                sale.getStatus(),
                sale.getCreatedAt(),
                sale.getUpdatedAt()
        );
    }

    private IntegrationPurchaseResponse toPurchaseResponse(Purchase purchase) {
        return new IntegrationPurchaseResponse(
                purchase.getId(),
                purchase.getCompany().getId(),
                purchase.getSupplier() == null ? null : purchase.getSupplier().getId(),
                purchase.getWarehouse() == null ? null : purchase.getWarehouse().getId(),
                purchase.getPurchaseNumber(),
                purchase.getPurchaseDate(),
                purchase.getSubtotal(),
                purchase.getTaxAmount(),
                purchase.getDiscountAmount(),
                purchase.getTotalAmount(),
                purchase.getStatus() == null ? null : purchase.getStatus().name(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }

    private IntegrationPaymentResponse toPaymentResponse(Payment payment) {
        return new IntegrationPaymentResponse(
                payment.getId(),
                payment.getCompany().getId(),
                payment.getPaymentNumber(),
                payment.getPaymentDate(),
                payment.getPaymentType(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getReferenceNumber(),
                payment.getNotes(),
                payment.getStatus(),
                payment.getCustomer() == null ? null : payment.getCustomer().getId(),
                payment.getSupplier() == null ? null : payment.getSupplier().getId(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private IntegrationInvoiceResponse toInvoiceResponse(Invoice invoice) {
        return new IntegrationInvoiceResponse(
                invoice.getId(),
                invoice.getCompany().getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getInvoiceType(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getDiscountAmount(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getNotes(),
                invoice.getCustomer() == null ? null : invoice.getCustomer().getId(),
                invoice.getSupplier() == null ? null : invoice.getSupplier().getId(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    private IntegrationPayrollResponse toPayrollResponse(Payroll payroll) {
        return new IntegrationPayrollResponse(
                payroll.getId(),
                payroll.getCompany().getId(),
                payroll.getEmployee().getId(),
                payroll.getPayrollYear(),
                payroll.getPayrollMonth(),
                payroll.getGrossSalary(),
                payroll.getDeductions(),
                payroll.getNetSalary(),
                payroll.getStatus() == null ? null : payroll.getStatus().name(),
                payroll.getCreatedAt(),
                payroll.getUpdatedAt()
        );
    }

    private IntegrationAuditLogResponse toAuditLogResponse(AuditLog auditLog) {
        return new IntegrationAuditLogResponse(
                auditLog.getId(),
                auditLog.getCompany().getId(),
                auditLog.getUser() == null ? null : auditLog.getUser().getId(),
                auditLog.getAction() == null ? null : auditLog.getAction().name(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDescription(),
                auditLog.getIpAddress(),
                auditLog.getCreatedAt()
        );
    }
}
