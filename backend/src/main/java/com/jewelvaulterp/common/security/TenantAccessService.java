package com.jewelvaulterp.common.security;

import com.jewelvaulterp.accounting.entity.Account;
import com.jewelvaulterp.accounting.entity.JournalEntry;
import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.employee.entity.Employee;
import com.jewelvaulterp.expense.entity.Expense;
import com.jewelvaulterp.expense.entity.ExpenseCategory;
import com.jewelvaulterp.gemstone.entity.Gemstone;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.invoice.entity.Invoice;
import com.jewelvaulterp.notification.entity.Notification;
import com.jewelvaulterp.payable.entity.Payable;
import com.jewelvaulterp.payroll.entity.Payroll;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.purity.entity.Purity;
import com.jewelvaulterp.receivable.entity.Receivable;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.sale.entity.Sale;
import com.jewelvaulterp.stockadjustment.entity.StockAdjustment;
import com.jewelvaulterp.stockmovement.entity.StockMovement;
import com.jewelvaulterp.stocktransfer.entity.StockTransfer;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.tax.entity.Tax;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TenantAccessService {

    private static final Map<String, Class<?>> ENTITY_TYPES = Map.ofEntries(
            Map.entry("companyId", Company.class),
            Map.entry("branchId", Branch.class),
            Map.entry("productId", Product.class),
            Map.entry("purityId", Purity.class),
            Map.entry("gemstoneId", Gemstone.class),
            Map.entry("warehouseId", Warehouse.class),
            Map.entry("customerId", Customer.class),
            Map.entry("supplierId", Supplier.class),
            Map.entry("employeeId", Employee.class),
            Map.entry("inventoryId", Inventory.class),
            Map.entry("movementId", StockMovement.class),
            Map.entry("adjustmentId", StockAdjustment.class),
            Map.entry("transferId", StockTransfer.class),
            Map.entry("saleId", Sale.class),
            Map.entry("purchaseId", com.jewelvaulterp.purchase.entity.Purchase.class),
            Map.entry("userId", User.class),
            Map.entry("roleId", Role.class),
            Map.entry("accountId", Account.class),
            Map.entry("journalEntryId", JournalEntry.class),
            Map.entry("expenseId", Expense.class),
            Map.entry("expenseCategoryId", ExpenseCategory.class),
            Map.entry("invoiceId", Invoice.class),
            Map.entry("payableId", Payable.class),
            Map.entry("payrollId", Payroll.class),
            Map.entry("receivableId", Receivable.class),
            Map.entry("notificationId", Notification.class),
            Map.entry("taxId", Tax.class)
    );

    private static final String[] COMPANY_RELATIONS = {
            "getCompany", "getBranch", "getWarehouse", "getFromWarehouse", "getToWarehouse",
            "getSourceWarehouse", "getDestinationWarehouse", "getInventory", "getUser", "getRole",
            "getCustomer", "getSupplier", "getProduct", "getSale", "getPurchase", "getInvoice",
            "getEmployee", "getTax", "getAccount", "getPayable", "getReceivable", "getExpense",
            "getCategory", "getJournalEntry"
    };
        private static final Map<String, Class<?>> TENANT_REFERENCE_IDS = Map.ofEntries(
            Map.entry("WarehouseId", Warehouse.class),
            Map.entry("BranchId", Branch.class),
            Map.entry("InventoryId", Inventory.class),
            Map.entry("ProductId", Product.class),
            Map.entry("CustomerId", Customer.class),
            Map.entry("SupplierId", Supplier.class),
            Map.entry("EmployeeId", Employee.class),
            Map.entry("UserId", User.class),
            Map.entry("RoleId", Role.class),
            Map.entry("AccountId", Account.class),
            Map.entry("SaleId", Sale.class),
            Map.entry("PurchaseId", com.jewelvaulterp.purchase.entity.Purchase.class),
            Map.entry("ExpenseId", Expense.class),
            Map.entry("InvoiceId", Invoice.class),
            Map.entry("TaxId", Tax.class)
        );

    private final EntityManager entityManager;

    public TenantAccessService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public UUID currentCompanyId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new AccessDeniedException("Tenant authentication is required.");
        }
        String companyId = jwtAuthentication.getToken().getClaimAsString("companyId");
        try {
            return UUID.fromString(companyId);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new AccessDeniedException("Tenant authentication is invalid.");
        }
    }

    public void requireCompany(String requestedCompanyId) {
        if (!currentCompanyId().toString().equals(requestedCompanyId)) {
            throw new AccessDeniedException("Access to another company is denied.");
        }
    }

    public void requireCompany(UUID requestedCompanyId) {
        if (!currentCompanyId().equals(requestedCompanyId)) {
            throw new AccessDeniedException("Access to another company is denied.");
        }
    }

    @Transactional
    public void requireEntityCompany(Class<?> entityType, UUID id) {
        Object entity = entityManager.find(entityType, id);
        if (entity != null) {
            companyIdOf(entity).ifPresent(this::requireCompany);
        }
    }

    public boolean belongsToCurrentCompany(Object value) {
        Optional<UUID> companyId = companyIdOf(value);
        return companyId.map(id -> id.equals(currentCompanyId())).orElseGet(() -> isSharedCatalogItem(value));
    }

    @Transactional
    public Optional<UUID> companyIdOf(Object value) {
        return companyIdOf(value, java.util.Collections.newSetFromMap(new IdentityHashMap<>()), 0);
    }

    public Optional<Class<?>> entityTypeFor(String variableName, Class<?> controllerType) {
        Class<?> explicit = ENTITY_TYPES.get(variableName);
        if (explicit != null) {
            return Optional.of(explicit);
        }
        if (!"id".equals(variableName)) {
            return Optional.empty();
        }
        String className = controllerType.getSimpleName().replace("Controller", "");
        String packageName = controllerType.getPackageName().replace(".controller", ".entity");
        try {
            return Optional.of(Class.forName(packageName + "." + className));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

    public boolean isSharedCatalogItem(Object value) {
        return value.getClass().getPackageName().contains(".permission.");
    }

    private Optional<UUID> companyIdOf(Object value, Set<Object> visited, int depth) {
        if (value == null || depth > 5 || !visited.add(value)) {
            return Optional.empty();
        }
        if (value instanceof Company company) {
            return Optional.of(company.getId());
        }
        if (value.getClass().getSimpleName().startsWith("Company")) {
            Optional<UUID> companyResponseId = invokeUuid(value, "getId");
            if (companyResponseId.isPresent()) {
                return companyResponseId;
            }
        }
        Optional<UUID> direct = invokeUuid(value, "getCompanyId").or(() -> invokeUuid(value, "companyId"));
        if (direct.isPresent()) {
            return direct;
        }
        for (String relation : COMPANY_RELATIONS) {
            Object related = invoke(value, relation).orElse(null);
            Optional<UUID> relatedCompany = companyIdOf(related, visited, depth + 1);
            if (relatedCompany.isPresent()) {
                return relatedCompany;
            }
        }
        for (Map.Entry<String, Class<?>> reference : TENANT_REFERENCE_IDS.entrySet()) {
            Optional<UUID> referenceId = invokeUuid(value, "get" + reference.getKey());
            if (referenceId.isPresent()) {
                Object related = entityManager.find(reference.getValue(), referenceId.get());
                Optional<UUID> relatedCompany = companyIdOf(related, visited, depth + 1);
                if (relatedCompany.isPresent()) {
                    return relatedCompany;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<UUID> invokeUuid(Object target, String methodName) {
        Object result = invoke(target, methodName).orElse(null);
        if (result instanceof UUID id) {
            return Optional.of(id);
        }
        if (result instanceof String id) {
            try {
                return Optional.of(UUID.fromString(id));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private Optional<Object> invoke(Object target, String methodName) {
        if (target == null) {
            return Optional.empty();
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return Optional.ofNullable(method.invoke(target));
        } catch (ReflectiveOperationException | SecurityException ignored) {
            return Optional.empty();
        }
    }
}