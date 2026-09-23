package com.jewelvaulterp.manufacturing.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.manufacturing.dto.CreateManufacturingOrderItemRequest;
import com.jewelvaulterp.manufacturing.dto.CreateManufacturingOrderRequest;
import com.jewelvaulterp.manufacturing.dto.ManufacturingOrderItemResponse;
import com.jewelvaulterp.manufacturing.dto.ManufacturingOrderResponse;
import com.jewelvaulterp.manufacturing.entity.ManufacturingItemType;
import com.jewelvaulterp.manufacturing.entity.ManufacturingOrder;
import com.jewelvaulterp.manufacturing.entity.ManufacturingOrderItem;
import com.jewelvaulterp.manufacturing.entity.ManufacturingOrderStatus;
import com.jewelvaulterp.manufacturing.repository.ManufacturingOrderItemRepository;
import com.jewelvaulterp.manufacturing.repository.ManufacturingOrderRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
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
public class ManufacturingOrderService {

    private final ManufacturingOrderRepository manufacturingOrderRepository;
    private final ManufacturingOrderItemRepository manufacturingOrderItemRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    public ManufacturingOrderService(
            ManufacturingOrderRepository manufacturingOrderRepository,
            ManufacturingOrderItemRepository manufacturingOrderItemRepository,
            CompanyRepository companyRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService
    ) {
        this.manufacturingOrderRepository = manufacturingOrderRepository;
        this.manufacturingOrderItemRepository =
                manufacturingOrderItemRepository;
        this.companyRepository = companyRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
    }

    public List<ManufacturingOrderResponse> getAllOrders() {
        return manufacturingOrderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ManufacturingOrderResponse getOrder(UUID id) {
        ManufacturingOrder order =
                manufacturingOrderRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Manufacturing order not found"
                                ));

        return toResponse(order);
    }

    @Transactional
    public ManufacturingOrderResponse createOrder(
            CreateManufacturingOrderRequest request
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

        if (manufacturingOrderRepository
                .findByCompanyIdAndOrderNumber(
                        request.companyId(),
                        request.orderNumber()
                )
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Manufacturing order number already exists"
            );
        }

        validateItems(request, company);

        LocalDateTime now = LocalDateTime.now();

        ManufacturingOrder order = new ManufacturingOrder(
                UUID.randomUUID(),
                company,
                warehouse,
                request.orderNumber(),
                now,
                ManufacturingOrderStatus.DRAFT,
                request.notes(),
                now,
                now
        );

        order = manufacturingOrderRepository.save(order);

        for (CreateManufacturingOrderItemRequest itemRequest :
                request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException(
                            "Product not found"
                    ));

            ManufacturingOrderItem item =
                    new ManufacturingOrderItem(
                            UUID.randomUUID(),
                            order,
                            product,
                            itemRequest.quantity(),
                            itemRequest.itemType()
                    );

            manufacturingOrderItemRepository.save(item);
        }

        return toResponse(order);
    }

    @Transactional
    public ManufacturingOrderResponse updateStatus(
            UUID id,
            ManufacturingOrderStatus status
    ) {
        ManufacturingOrder order =
                manufacturingOrderRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Manufacturing order not found"
                                ));

        if (order.getStatus()
                == ManufacturingOrderStatus.COMPLETED) {

            throw new IllegalArgumentException(
                    "Completed manufacturing order cannot change status"
            );
        }

        List<ManufacturingOrderItem> items =
                manufacturingOrderItemRepository
                        .findByManufacturingOrderId(id);

        if (status == ManufacturingOrderStatus.IN_PROGRESS) {

            if (items.isEmpty()) {
                throw new IllegalArgumentException(
                        "Manufacturing order must contain at least one item"
                );
            }

            validateRawMaterials(order, items);
        }

        if (status == ManufacturingOrderStatus.COMPLETED) {

            if (items.isEmpty()) {
                throw new IllegalArgumentException(
                        "Manufacturing order must contain at least one item"
                );
            }

            validateRawMaterials(order, items);

            processManufacturing(order, items);
        }

        order.setStatus(status);

        return toResponse(
                manufacturingOrderRepository.save(order)
        );
    }

    private void validateItems(
            CreateManufacturingOrderRequest request,
            Company company
    ) {
        for (CreateManufacturingOrderItemRequest itemRequest :
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
        }

        boolean hasRawMaterial = request.items()
                .stream()
                .anyMatch(item ->
                        item.itemType()
                                == ManufacturingItemType.RAW_MATERIAL);

        boolean hasFinishedProduct = request.items()
                .stream()
                .anyMatch(item ->
                        item.itemType()
                                == ManufacturingItemType.FINISHED_PRODUCT);

        if (!hasRawMaterial || !hasFinishedProduct) {
            throw new IllegalArgumentException(
                    "Manufacturing order must contain raw materials and finished products"
            );
        }
    }

    private void validateRawMaterials(
            ManufacturingOrder order,
            List<ManufacturingOrderItem> items
    ) {
        for (ManufacturingOrderItem item : items) {

            if (item.getItemType()
                    != ManufacturingItemType.RAW_MATERIAL) {
                continue;
            }

            Inventory inventory =
                    inventoryRepository
                            .findByWarehouseIdAndProductId(
                                    order.getWarehouse().getId(),
                                    item.getProduct().getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Inventory not found for raw material: "
                                                    + item.getProduct().getSku()
                                    ));

            if (inventory.getAvailableQuantity()
                    .compareTo(item.getQuantity()) < 0) {

                throw new IllegalArgumentException(
                        "Insufficient raw material inventory for product: "
                                + item.getProduct().getSku()
                );
            }
        }
    }

    private void processManufacturing(
            ManufacturingOrder order,
            List<ManufacturingOrderItem> items
    ) {
        for (ManufacturingOrderItem item : items) {

            Inventory inventory =
                    inventoryRepository
                            .findByWarehouseIdAndProductId(
                                    order.getWarehouse().getId(),
                                    item.getProduct().getId()
                            )
                            .orElse(null);

            if (inventory == null) {

                LocalDateTime now = LocalDateTime.now();

                inventory = inventoryRepository.save(
                        new Inventory(
                                UUID.randomUUID(),
                                order.getWarehouse(),
                                item.getProduct(),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                now,
                                now
                        )
                );
            }

            MovementType movementType =
                    item.getItemType()
                            == ManufacturingItemType.RAW_MATERIAL
                            ? MovementType.MANUFACTURING_OUT
                            : MovementType.MANUFACTURING_IN;

            stockMovementService.createMovement(
                    new CreateStockMovementRequest(
                            inventory.getId(),
                            movementType,
                            item.getQuantity(),
                            order.getOrderNumber(),
                            order.getNotes(),
                            order.getOrderDate()
                    )
            );
        }
    }

    private ManufacturingOrderResponse toResponse(
            ManufacturingOrder order
    ) {
        List<ManufacturingOrderItemResponse> items =
                manufacturingOrderItemRepository
                        .findByManufacturingOrderId(order.getId())
                        .stream()
                        .map(item ->
                                new ManufacturingOrderItemResponse(
                                        item.getId(),
                                        item.getProduct().getId(),
                                        item.getQuantity(),
                                        item.getItemType()
                                )
                        )
                        .toList();

        return new ManufacturingOrderResponse(
                order.getId(),
                order.getCompany().getId(),
                order.getWarehouse().getId(),
                order.getOrderNumber(),
                order.getOrderDate(),
                order.getStatus(),
                order.getNotes(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}