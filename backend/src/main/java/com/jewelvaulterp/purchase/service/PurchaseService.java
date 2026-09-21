package com.jewelvaulterp.purchase.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.purchase.dto.CreatePurchaseItemRequest;
import com.jewelvaulterp.purchase.dto.CreatePurchaseRequest;
import com.jewelvaulterp.purchase.dto.PurchaseItemResponse;
import com.jewelvaulterp.purchase.dto.PurchaseResponse;
import com.jewelvaulterp.purchase.entity.Purchase;
import com.jewelvaulterp.purchase.entity.PurchaseItem;
import com.jewelvaulterp.purchase.entity.PurchaseStatus;
import com.jewelvaulterp.purchase.repository.PurchaseItemRepository;
import com.jewelvaulterp.purchase.repository.PurchaseRepository;
import com.jewelvaulterp.stockmovement.dto.CreateStockMovementRequest;
import com.jewelvaulterp.stockmovement.entity.MovementType;
import com.jewelvaulterp.stockmovement.service.StockMovementService;
import com.jewelvaulterp.supplier.entity.Supplier;
import com.jewelvaulterp.supplier.repository.SupplierRepository;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final CompanyRepository companyRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository,
            CompanyRepository companyRepository,
            SupplierRepository supplierRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService
    ) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.companyRepository = companyRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
    }

    public List<PurchaseResponse> getAllPurchases() {
        return purchaseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PurchaseResponse getPurchase(UUID id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Purchase not found"));

        return toResponse(purchase);
    }

    @Transactional
    public PurchaseResponse createPurchase(
            CreatePurchaseRequest request
    ) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Supplier not found"));

        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Warehouse not found"));

        if (!supplier.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Supplier does not belong to company"
            );
        }

        if (!warehouse.getBranch().getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Warehouse does not belong to company"
            );
        }

        if (purchaseRepository.findByCompanyIdAndPurchaseNumber(
                request.companyId(),
                request.purchaseNumber()
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "Purchase number already exists"
            );
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreatePurchaseItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Product not found: " + itemRequest.productId()
                    ));

            if (product.getCompany().getId().equals(company.getId()) == false) {
                throw new IllegalArgumentException(
                        "Product does not belong to company"
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
                    "Purchase total cannot be negative"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Purchase purchase = new Purchase(
                UUID.randomUUID(),
                company,
                supplier,
                warehouse,
                request.purchaseNumber(),
                request.purchaseDate() != null
                        ? request.purchaseDate()
                        : now,
                subtotal,
                request.taxAmount(),
                request.discountAmount(),
                total,
                PurchaseStatus.DRAFT,
                now,
                now
        );

        purchase = purchaseRepository.save(purchase);

        for (CreatePurchaseItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException("Product not found"));

            BigDecimal itemTotal = itemRequest.unitPrice()
                    .multiply(itemRequest.quantity());

            PurchaseItem item = new PurchaseItem(
                    UUID.randomUUID(),
                    purchase,
                    product,
                    itemRequest.quantity(),
                    itemRequest.unitPrice(),
                    itemTotal
            );

            purchaseItemRepository.save(item);
        }

        return toResponse(purchase);
    }

    @Transactional
    public PurchaseResponse updateStatus(
            UUID id,
            PurchaseStatus status
    ) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Purchase not found"));

        if (purchase.getStatus() == PurchaseStatus.RECEIVED) {
            throw new IllegalArgumentException(
                    "Received purchase cannot change status"
            );
        }

        if (status == PurchaseStatus.RECEIVED) {

            List<PurchaseItem> items =
                    purchaseItemRepository.findByPurchaseId(id);

            for (PurchaseItem item : items) {

                Inventory inventory = inventoryRepository
                        .findByWarehouseIdAndProductId(
                                purchase.getWarehouse().getId(),
                                item.getProduct().getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Inventory not found for warehouse and product"
                                ));

                stockMovementService.createMovement(
                        new CreateStockMovementRequest(
                                inventory.getId(),
                                MovementType.PURCHASE,
                                item.getQuantity(),
                                purchase.getPurchaseNumber(),
                                "Purchase received",
                                purchase.getPurchaseDate()
                        )
                );
            }
        }

        purchase.setStatus(status);

        return toResponse(purchaseRepository.save(purchase));
    }

    private PurchaseResponse toResponse(Purchase purchase) {

        List<PurchaseItemResponse> items =
                purchaseItemRepository
                        .findByPurchaseId(purchase.getId())
                        .stream()
                        .map(item -> new PurchaseItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getTotalPrice()
                        ))
                        .toList();

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getCompany().getId(),
                purchase.getSupplier().getId(),
                purchase.getWarehouse().getId(),
                purchase.getPurchaseNumber(),
                purchase.getPurchaseDate(),
                purchase.getSubtotal(),
                purchase.getTaxAmount(),
                purchase.getDiscountAmount(),
                purchase.getTotalAmount(),
                purchase.getStatus(),
                items,
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }
}