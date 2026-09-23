package com.jewelvaulterp.sale.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.customer.entity.Customer;
import com.jewelvaulterp.customer.repository.CustomerRepository;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.sale.dto.CreateSaleItemRequest;
import com.jewelvaulterp.sale.dto.CreateSaleRequest;
import com.jewelvaulterp.sale.dto.SaleItemResponse;
import com.jewelvaulterp.sale.dto.SaleResponse;
import com.jewelvaulterp.sale.entity.Sale;
import com.jewelvaulterp.sale.entity.SaleItem;
import com.jewelvaulterp.sale.entity.SaleStatus;
import com.jewelvaulterp.sale.repository.SaleItemRepository;
import com.jewelvaulterp.sale.repository.SaleRepository;
import com.jewelvaulterp.stockmovement.dto.CreateStockMovementRequest;
import com.jewelvaulterp.stockmovement.entity.MovementType;
import com.jewelvaulterp.stockmovement.service.StockMovementService;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    public SaleService(
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            CompanyRepository companyRepository,
            CustomerRepository customerRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService
    ) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
    }

    public List<SaleResponse> getAllSales() {
        return saleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SaleResponse getSale(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Sale not found"));

        return toResponse(sale);
    }

    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {

        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found"));

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Warehouse not found"));

        if (!customer.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Customer does not belong to company"
            );
        }

        if (!warehouse.getBranch().getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Warehouse does not belong to company"
            );
        }

        if (saleRepository.findByCompanyIdAndSaleNumber(
                request.companyId(),
                request.saleNumber()
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "Sale number already exists"
            );
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateSaleItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException("Product not found"));

            if (!product.getCompany().getId().equals(company.getId())) {
                throw new IllegalArgumentException(
                        "Product does not belong to company"
                );
            }

            Inventory inventory = inventoryRepository
                    .findByWarehouseIdAndProductId(
                            warehouse.getId(),
                            product.getId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Inventory not found for warehouse and product"
                            ));

            BigDecimal availableQuantity =
                    inventory.getAvailableQuantity();

            if (availableQuantity.compareTo(itemRequest.quantity()) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient inventory for product: "
                                + product.getSku()
                );
            }

            subtotal = subtotal.add(
                    itemRequest.unitPrice()
                            .multiply(itemRequest.quantity())
            );
        }

        BigDecimal total = subtotal
                .add(request.taxAmount())
                .subtract(request.discountAmount());

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Sale total cannot be negative"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Sale sale = new Sale(
                UUID.randomUUID(),
                company,
                customer,
                warehouse,
                request.saleNumber(),
                request.saleDate() != null
                        ? request.saleDate()
                        : now,
                subtotal,
                request.taxAmount(),
                request.discountAmount(),
                total,
                SaleStatus.DRAFT,
                now,
                now
        );

        sale = saleRepository.save(sale);

        for (CreateSaleItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException("Product not found"));

            BigDecimal itemTotal = itemRequest.unitPrice()
                    .multiply(itemRequest.quantity());

            SaleItem item = new SaleItem(
                    UUID.randomUUID(),
                    sale,
                    product,
                    itemRequest.quantity(),
                    itemRequest.unitPrice(),
                    itemTotal
            );

            saleItemRepository.save(item);
        }

        return toResponse(sale);
    }

    @Transactional
    public SaleResponse updateStatus(
            UUID id,
            SaleStatus status
    ) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Sale not found"));

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Completed sale cannot change status"
            );
        }

        if (status == SaleStatus.COMPLETED) {

            List<SaleItem> items =
                    saleItemRepository.findBySaleId(id);

            for (SaleItem item : items) {

                Inventory inventory = inventoryRepository
                        .findByWarehouseIdAndProductId(
                                sale.getWarehouse().getId(),
                                item.getProduct().getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Inventory not found for warehouse and product"
                                ));

                if (inventory.getAvailableQuantity()
                        .compareTo(item.getQuantity()) < 0) {
                    throw new IllegalArgumentException(
                            "Insufficient inventory for product: "
                                    + item.getProduct().getSku()
                    );
                }

                stockMovementService.createMovement(
                        new CreateStockMovementRequest(
                                inventory.getId(),
                                MovementType.SALE,
                                item.getQuantity(),
                                sale.getSaleNumber(),
                                "Sale completed",
                                sale.getSaleDate()
                        )
                );
            }
        }

        sale.setStatus(status);

        return toResponse(saleRepository.save(sale));
    }

    private SaleResponse toResponse(Sale sale) {

        List<SaleItemResponse> items =
                saleItemRepository
                        .findBySaleId(sale.getId())
                        .stream()
                        .map(item -> new SaleItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getTotalPrice()
                        ))
                        .toList();

        return new SaleResponse(
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
                items,
                sale.getCreatedAt(),
                sale.getUpdatedAt()
        );
    }
}