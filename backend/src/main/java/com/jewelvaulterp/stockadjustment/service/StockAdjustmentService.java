package com.jewelvaulterp.stockadjustment.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.stockadjustment.dto.CreateStockAdjustmentItemRequest;
import com.jewelvaulterp.stockadjustment.dto.CreateStockAdjustmentRequest;
import com.jewelvaulterp.stockadjustment.dto.StockAdjustmentItemResponse;
import com.jewelvaulterp.stockadjustment.dto.StockAdjustmentResponse;
import com.jewelvaulterp.stockadjustment.entity.AdjustmentType;
import com.jewelvaulterp.stockadjustment.entity.StockAdjustment;
import com.jewelvaulterp.stockadjustment.entity.StockAdjustmentItem;
import com.jewelvaulterp.stockadjustment.entity.StockAdjustmentStatus;
import com.jewelvaulterp.stockadjustment.repository.StockAdjustmentItemRepository;
import com.jewelvaulterp.stockadjustment.repository.StockAdjustmentRepository;
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
public class StockAdjustmentService {

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAdjustmentItemRepository stockAdjustmentItemRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    public StockAdjustmentService(
            StockAdjustmentRepository stockAdjustmentRepository,
            StockAdjustmentItemRepository stockAdjustmentItemRepository,
            CompanyRepository companyRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService
    ) {
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.stockAdjustmentItemRepository =
                stockAdjustmentItemRepository;
        this.companyRepository = companyRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
    }

    public List<StockAdjustmentResponse> getAllAdjustments() {
        return stockAdjustmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StockAdjustmentResponse getAdjustment(UUID id) {
        StockAdjustment adjustment =
                stockAdjustmentRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Stock adjustment not found"
                                ));

        return toResponse(adjustment);
    }

    @Transactional
    public StockAdjustmentResponse createAdjustment(
            CreateStockAdjustmentRequest request
    ) {
        Company company = companyRepository.findById(
                request.companyId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Company not found"));

        Warehouse warehouse = warehouseRepository.findById(
                request.warehouseId()
        ).orElseThrow(() ->
                new IllegalArgumentException("Warehouse not found"));

        if (!warehouse.getBranch()
                .getCompany()
                .getId()
                .equals(company.getId())) {

            throw new IllegalArgumentException(
                    "Warehouse does not belong to company"
            );
        }

        if (stockAdjustmentRepository
                .findByCompanyIdAndAdjustmentNumber(
                        request.companyId(),
                        request.adjustmentNumber()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Adjustment number already exists"
            );
        }

        for (CreateStockAdjustmentItemRequest itemRequest :
                request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Product not found"
                    ));

            if (!product.getCompany()
                    .getId()
                    .equals(company.getId())) {

                throw new IllegalArgumentException(
                        "Product does not belong to company"
                );
            }

            if (itemRequest.adjustmentType()
                    == AdjustmentType.ADJUSTMENT_OUT) {

                Inventory inventory =
                        inventoryRepository
                                .findByWarehouseIdAndProductId(
                                        warehouse.getId(),
                                        product.getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Inventory not found for product: "
                                                        + product.getSku()
                                        ));

                if (inventory.getAvailableQuantity()
                        .compareTo(itemRequest.quantity()) < 0) {

                    throw new IllegalArgumentException(
                            "Insufficient available inventory for product: "
                                    + product.getSku()
                    );
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();

        StockAdjustment adjustment = new StockAdjustment(
                UUID.randomUUID(),
                company,
                warehouse,
                request.adjustmentNumber(),
                now,
                StockAdjustmentStatus.DRAFT,
                request.notes(),
                now,
                now
        );

        adjustment =
                stockAdjustmentRepository.save(adjustment);

        for (CreateStockAdjustmentItemRequest itemRequest :
                request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Product not found"
                    ));

            StockAdjustmentItem item =
                    new StockAdjustmentItem(
                            UUID.randomUUID(),
                            adjustment,
                            product,
                            itemRequest.quantity(),
                            itemRequest.adjustmentType()
                    );

            stockAdjustmentItemRepository.save(item);
        }

        return toResponse(adjustment);
    }

    @Transactional
    public StockAdjustmentResponse updateStatus(
            UUID id,
            StockAdjustmentStatus status
    ) {
        StockAdjustment adjustment =
                stockAdjustmentRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Stock adjustment not found"
                                ));

        if (adjustment.getStatus()
                == StockAdjustmentStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Completed adjustment cannot change status"
            );
        }

        if (status == StockAdjustmentStatus.COMPLETED) {

            List<StockAdjustmentItem> items =
                    stockAdjustmentItemRepository
                            .findByAdjustmentId(id);

            if (items.isEmpty()) {
                throw new IllegalArgumentException(
                        "Stock adjustment must contain at least one item"
                );
            }

            for (StockAdjustmentItem item : items) {

                Inventory inventory =
                        inventoryRepository
                                .findByWarehouseIdAndProductId(
                                        adjustment.getWarehouse().getId(),
                                        item.getProduct().getId()
                                )
                                .orElse(null);

                if (item.getAdjustmentType()
                        == AdjustmentType.ADJUSTMENT_OUT) {

                    if (inventory == null) {
                        throw new IllegalArgumentException(
                                "Inventory not found for product: "
                                        + item.getProduct().getSku()
                        );
                    }

                    if (inventory.getAvailableQuantity()
                            .compareTo(item.getQuantity()) < 0) {

                        throw new IllegalArgumentException(
                                "Insufficient available inventory for product: "
                                        + item.getProduct().getSku()
                        );
                    }
                }

                if (inventory == null) {

                    LocalDateTime now = LocalDateTime.now();

                    inventory = inventoryRepository.save(
                            new Inventory(
                                    UUID.randomUUID(),
                                    adjustment.getWarehouse(),
                                    item.getProduct(),
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO,
                                    now,
                                    now
                            )
                    );
                }

                MovementType movementType =
                        item.getAdjustmentType()
                                == AdjustmentType.ADJUSTMENT_IN
                                ? MovementType.ADJUSTMENT_IN
                                : MovementType.ADJUSTMENT_OUT;

                stockMovementService.createMovement(
                        new CreateStockMovementRequest(
                                inventory.getId(),
                                movementType,
                                item.getQuantity(),
                                adjustment.getAdjustmentNumber(),
                                adjustment.getNotes(),
                                adjustment.getAdjustmentDate()
                        )
                );
            }
        }

        adjustment.setStatus(status);

        return toResponse(
                stockAdjustmentRepository.save(adjustment)
        );
    }

    private StockAdjustmentResponse toResponse(
            StockAdjustment adjustment
    ) {
        List<StockAdjustmentItemResponse> items =
                stockAdjustmentItemRepository
                        .findByAdjustmentId(adjustment.getId())
                        .stream()
                        .map(item ->
                                new StockAdjustmentItemResponse(
                                        item.getId(),
                                        item.getProduct().getId(),
                                        item.getQuantity(),
                                        item.getAdjustmentType()
                                )
                        )
                        .toList();

        return new StockAdjustmentResponse(
                adjustment.getId(),
                adjustment.getCompany().getId(),
                adjustment.getWarehouse().getId(),
                adjustment.getAdjustmentNumber(),
                adjustment.getAdjustmentDate(),
                adjustment.getStatus(),
                adjustment.getNotes(),
                items,
                adjustment.getCreatedAt(),
                adjustment.getUpdatedAt()
        );
    }
}