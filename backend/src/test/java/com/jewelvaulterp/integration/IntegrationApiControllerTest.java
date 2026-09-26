package com.jewelvaulterp.integration;

import com.jewelvaulterp.accounting.entity.Account;
import tools.jackson.databind.ObjectMapper;
import com.jewelvaulterp.accounting.entity.AccountType;
import com.jewelvaulterp.accounting.repository.AccountRepository;
import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.branch.repository.BranchRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.customer.repository.CustomerRepository;
import com.jewelvaulterp.employee.entity.Employee;
import com.jewelvaulterp.employee.repository.EmployeeRepository;
import com.jewelvaulterp.payment.entity.Payment;
import com.jewelvaulterp.payment.entity.PaymentMethod;
import com.jewelvaulterp.payment.entity.PaymentStatus;
import com.jewelvaulterp.payment.entity.PaymentType;
import com.jewelvaulterp.payment.repository.PaymentRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.sale.entity.Sale;
import com.jewelvaulterp.sale.entity.SaleStatus;
import com.jewelvaulterp.sale.repository.SaleRepository;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.supplier.repository.SupplierRepository;
import com.jewelvaulterp.tax.entity.Tax;
import com.jewelvaulterp.tax.entity.TaxType;
import com.jewelvaulterp.tax.repository.TaxRepository;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class IntegrationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TaxRepository taxRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Company companyA;
    private Company companyB;
    private Branch branchA;
    private Warehouse warehouseA;

    @BeforeEach
    void setUp() {
        companyRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        supplierRepository.deleteAll();
        saleRepository.deleteAll();
        paymentRepository.deleteAll();
        accountRepository.deleteAll();
        taxRepository.deleteAll();
        branchRepository.deleteAll();
        warehouseRepository.deleteAll();
        employeeRepository.deleteAll();

        companyA = companyRepository.save(new Company(
                UUID.randomUUID(),
                "Acme Gold",
                "Acme Gold Ltd",
                "US",
                "USD",
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(2)
        ));
        companyB = companyRepository.save(new Company(
                UUID.randomUUID(),
                "Blue Gem",
                "Blue Gem Pvt Ltd",
                "IN",
                "INR",
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(1)
        ));

        branchA = branchRepository.save(new Branch(
                UUID.randomUUID(),
                companyA,
                "Main Branch",
                "BR-01",
                "123 Market St",
                "New York",
                "US",
                "555-0100",
                "ops@acme.com",
                true,
                LocalDateTime.now().minusDays(8),
                LocalDateTime.now().minusDays(2)
        ));

        warehouseA = warehouseRepository.save(new Warehouse(
                UUID.randomUUID(),
                branchA,
                "Main Warehouse",
                "WH-01",
                "200 Storage Lane",
                "Inventory storage",
                true,
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now().minusDays(1)
        ));

        productRepository.save(new Product(
                UUID.randomUUID(),
                companyA,
                "SKU-001",
                "Gold Ring",
                "Ring",
                "Gold",
                "22K",
                new BigDecimal("10.500"),
                new BigDecimal("9.750"),
                new BigDecimal("150.00"),
                true,
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)
        ));

        productRepository.save(new Product(
                UUID.randomUUID(),
                companyB,
                "SKU-999",
                "Other Ring",
                "Ring",
                "Gold",
                "18K",
                new BigDecimal("8.500"),
                new BigDecimal("7.750"),
                new BigDecimal("100.00"),
                true,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1)
        ));

        customerRepository.save(new Customer(
                UUID.randomUUID(),
                companyA,
                "Alpha Retailer",
                "CUST-01",
                "555-1111",
                "alpha@example.com",
                "Alpha Lane",
                "TAX-001",
                true,
                LocalDateTime.now().minusDays(9),
                LocalDateTime.now().minusDays(1)
        ));

        customerRepository.save(new Customer(
                UUID.randomUUID(),
                companyB,
                "Beta Retailer",
                "CUST-99",
                "555-9999",
                "beta@example.com",
                "Beta Lane",
                "TAX-099",
                true,
                LocalDateTime.now().minusDays(9),
                LocalDateTime.now().minusDays(1)
        ));

        supplierRepository.save(new Supplier(
                UUID.randomUUID(),
                companyA,
                "Premium Metals",
                "SUP-01",
                "Jane Doe",
                "555-2200",
                "supplier@example.com",
                "1 Supplier Rd",
                "VAT-001",
                true,
                LocalDateTime.now().minusDays(11),
                LocalDateTime.now().minusDays(2)
        ));

        Customer customerA = customerRepository.findAll().stream()
                .filter(c -> c.getCompany().getId().equals(companyA.getId()))
                .findFirst()
                .orElseThrow();

        saleRepository.save(new Sale(
                UUID.randomUUID(),
                companyA,
                customerA,
                warehouseA,
                "SALE-001",
                LocalDateTime.now().minusDays(2),
                new BigDecimal("500.00"),
                new BigDecimal("25.00"),
                new BigDecimal("10.00"),
                new BigDecimal("515.00"),
                SaleStatus.COMPLETED,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1)
        ));

        paymentRepository.save(new Payment(
                UUID.randomUUID(),
                companyA,
                "PAY-001",
                LocalDateTime.now().minusDays(1),
                PaymentType.CUSTOMER_PAYMENT,
                PaymentMethod.CASH,
                new BigDecimal("515.00"),
                "REF-100",
                "Settlement",
                PaymentStatus.COMPLETED,
                customerA,
                null,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1)
        ));

        accountRepository.save(new Account(
                UUID.randomUUID(),
                companyA,
                "1100",
                "Cash",
                AccountType.ASSET,
                true,
                LocalDateTime.now().minusDays(6),
                LocalDateTime.now().minusDays(1)
        ));

        taxRepository.save(new Tax(
                companyA,
                "VAT",
                "VAT-01",
                TaxType.SALES,
                new BigDecimal("0.2000"),
                "Standard sales VAT"
        ));

        employeeRepository.save(new Employee(
                companyA,
                branchA,
                "EMP-001",
                "Alice",
                "Smith",
                "alice@example.com",
                "555-3333",
                "Main street",
                "Sales",
                "Manager",
                LocalDate.now().minusYears(2)
        ));
    }

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/integration/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service", is("JewelVaultERP")))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidCompanyReturnsNotFound() throws Exception {
        UUID missingCompanyId = UUID.randomUUID();

        mockMvc.perform(get("/api/integration/{companyId}/products", missingCompanyId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Company not found")));
    }

    @Test
    void productExportReturnsOnlyCompanyRecords() throws Exception {
        mockMvc.perform(get("/api/integration/{companyId}/products", companyA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].companyId", is(companyA.getId().toString())))
                .andExpect(jsonPath("$.content[0].sku", is("SKU-001")))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(100)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    void customerExportAndCompanyIsolation() throws Exception {
        mockMvc.perform(get("/api/integration/{companyId}/customers", companyA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].companyId", is(companyA.getId().toString())))
                .andExpect(jsonPath("$.content[0].code", is("CUST-01")));

        mockMvc.perform(get("/api/integration/{companyId}/customers", companyB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].companyId", is(companyB.getId().toString())));
    }

    @Test
    void supplierExportAndSalesExport() throws Exception {
        mockMvc.perform(get("/api/integration/{companyId}/suppliers", companyA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].code", is("SUP-01")));

        mockMvc.perform(get("/api/integration/{companyId}/sales", companyA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].saleNumber", is("SALE-001")))
                .andExpect(jsonPath("$.content[0].companyId", is(companyA.getId().toString())));
    }

    @Test
    void paginationDefaultsAndMaximumSizeEnforced() throws Exception {
        mockMvc.perform(get("/api/integration/{companyId}/payments", companyA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(100)))
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/integration/{companyId}/payments", companyA.getId()).param("size", "1001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Maximum page size")));
    }

    @Test
    void updatedSinceFiltersRecords() throws Exception {
        LocalDateTime since = LocalDateTime.now().minusDays(2);

        mockMvc.perform(get("/api/integration/{companyId}/products", companyA.getId())
                        .param("updatedSince", since.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/integration/{companyId}/audit-logs", companyA.getId())
                        .param("updatedSince", since.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void dtoMappingIncludesExpectedFields() throws Exception {
        mockMvc.perform(get("/api/integration/{companyId}/customers", companyA.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("Alpha Retailer")))
                .andExpect(jsonPath("$.content[0].active", is(true)))
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.content[0].updatedAt").exists());
    }

    @Test
    void errorHandlingReturnsConsistentStructure() throws Exception {
        UUID missingCompanyId = UUID.randomUUID();

        mockMvc.perform(get("/api/integration/{companyId}/products", missingCompanyId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.path", containsString("/api/integration/")))
                .andExpect(jsonPath("$.message", containsString("Company not found")));
    }
}
