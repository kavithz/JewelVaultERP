package com.jewelvaulterp.stocktransfer.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.stockmovement.dto.CreateStockMovementRequest;
import com.jewelvaulterp.stockmovement.entity.MovementType;
import com.jewelvaulterp.stockmovement.service.StockMovementService;
import com.jewelvaulterp.stocktransfer.dto.CreateStockTransferItemRequest;
import com.jewelvaulterp.stocktransfer.dto.CreateStockTransferRequest;
import com.jewelvaulterp.stocktransfer.dto.StockTransferItemResponse;
import com.jewelvaulterp.stocktransfer.dto.StockTransferResponse;
import com.jewelvaulterp.stocktransfer.entity.StockTransfer;
import com.jewelvaulterp.stocktransfer.entity.StockTransferItem;
import com.jewelvaulterp.stocktransfer.entity.StockTransferStatus;
import com.jewelvaulterp.stocktransfer.repository.StockTransferItemRepository;
import com.jewelvaulterp.stocktransfer.repository.StockTransferRepository;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final StockTransferItemRepository stockTransferItemRepository;
    private final CompanyRepository companyRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementService stockMovementService;

    public StockTransferService(
            StockTransferRepository stockTransferRepository,
            StockTransferItemRepository stockTransferItemRepository,
            CompanyRepository companyRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementService stockMovementService
    ) {
        this.stockTransferRepository = stockTransferRepository;
        this.stockTransferItemRepository = stockTransferItemRepository;
        this.companyRepository = companyRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementService = stockMovementService;
    }

    public List<StockTransferResponse> getAllTransfers() {
        return stockTransferRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StockTransferResponse getTransfer(UUID id) {
        StockTransfer transfer = stockTransferRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock transfer not found"));

        return toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse createTransfer(
            CreateStockTransferRequest request
    ) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found"));

        Warehouse sourceWarehouse = warehouseRepository
                .findById(request.sourceWarehouseId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Source warehouse not found"
                        ));

        Warehouse destinationWarehouse = warehouseRepository
                .findById(request.destinationWarehouseId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Destination warehouse not found"
                        ));

        if (sourceWarehouse.getId().equals(destinationWarehouse.getId())) {
            throw new IllegalArgumentException(
                    "Source and destination warehouses must be different"
            );
        }

        if (!sourceWarehouse.getBranch().getCompany().getId()
                .equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Source warehouse does not belong to company"
            );
        }

        if (!destinationWarehouse.getBranch().getCompany().getId()
                .equals(company.getId())) {
            throw new IllegalArgumentException(
                    "Destination warehouse does not belong to company"
            );
        }

        if (stockTransferRepository
                .findByCompanyIdAndTransferNumber(
                        request.companyId(),
                        request.transferNumber()
                )
                .isPresent()) {
            throw new IllegalArgumentException(
                    "Transfer number already exists"
            );
        }

        for (CreateStockTransferItemRequest itemRequest : request.items()) {

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
                            sourceWarehouse.getId(),
                            product.getId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Source inventory not found for product: "
                                            + product.getSku()
                            ));

            if (inventory.getAvailableQuantity()
                    .compareTo(itemRequest.quantity()) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient inventory for product: "
                                + product.getSku()
                );
            }
        }

        LocalDateTime now = LocalDateTime.now();

        StockTransfer transfer = new StockTransfer(
                UUID.randomUUID(),
                company,
                sourceWarehouse,
                destinationWarehouse,
                request.transferNumber(),
                request.transferDate() != null
                        ? request.transferDate()
                        : now,
                StockTransferStatus.DRAFT,
                request.notes(),
                now,
                now
        );

        transfer = stockTransferRepository.save(transfer);

        for (CreateStockTransferItemRequest itemRequest : request.items()) {

            Product product = productRepository.findById(
                    itemRequest.productId()
            ).orElseThrow(() ->
                    new IllegalArgumentException("Product not found"));

            StockTransferItem item = new StockTransferItem(
                    UUID.randomUUID(),
                    transfer,
                    product,
                    itemRequest.quantity()
            );

            stockTransferItemRepository.save(item);
        }

        return toResponse(transfer);
    }

    @Transactional
    public StockTransferResponse updateStatus(
            UUID id,
            StockTransferStatus status
    ) {
        StockTransfer transfer = stockTransferRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock transfer not found"));

        if (transfer.getStatus() == StockTransferStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Completed transfer cannot change status"
            );
        }

        if (status == StockTransferStatus.COMPLETED) {

            List<StockTransferItem> items =
                    stockTransferItemRepository.findByTransferId(id);

            for (StockTransferItem item : items) {

                Inventory sourceInventory =
                        inventoryRepository
                                .findByWarehouseIdAndProductId(
                                        transfer.getSourceWarehouse().getId(),
                                        item.getProduct().getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Source inventory not found"
                                        ));

                if (sourceInventory.getAvailableQuantity()
                        .compareTo(item.getQuantity()) < 0) {
                    throw new IllegalArgumentException(
                            "Insufficient inventory for product: "
                                    + item.getProduct().getSku()
                    );
                }

                Inventory destinationInventory =
                        inventoryRepository
                                .findByWarehouseIdAndProductId(
                                        transfer.getDestinationWarehouse().getId(),
                                        item.getProduct().getId()
                                )
                                .orElseGet(() -> {
                                    LocalDateTime now = LocalDateTime.now();

                                    return inventoryRepository.save(
                                            new Inventory(
                                                    UUID.randomUUID(),
                                                    transfer.getDestinationWarehouse(),
                                                    item.getProduct(),
                                                    BigDecimal.ZERO,
                                                    BigDecimal.ZERO,
                                                    now,
                                                    now
                                            )
                                    );
                                });

                stockMovementService.createMovement(
                        new CreateStockMovementRequest(
                                sourceInventory.getId(),
                                MovementType.TRANSFER_OUT,
                                item.getQuantity(),
                                transfer.getTransferNumber(),
                                "Stock transfer out",
                                transfer.getTransferDate()
                        )
                );

                stockMovementService.createMovement(
                        new CreateStockMovementRequest(
                                destinationInventory.getId(),
                                MovementType.TRANSFER_IN,
                                item.getQuantity(),
                                transfer.getTransferNumber(),
                                "Stock transfer in",
                                transfer.getTransferDate()
                        )
                );
            }
        }

        transfer.setStatus(status);

        return toResponse(
                stockTransferRepository.save(transfer)
        );
    }

    private StockTransferResponse toResponse(
            StockTransfer transfer
    ) {
        List<StockTransferItemResponse> items =
                stockTransferItemRepository
                        .findByTransferId(transfer.getId())
                        .stream()
                        .map(item -> new StockTransferItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity()
                        ))
                        .toList();

        return new StockTransferResponse(
                transfer.getId(),
                transfer.getCompany().getId(),
                transfer.getSourceWarehouse().getId(),
                transfer.getDestinationWarehouse().getId(),
                transfer.getTransferNumber(),
                transfer.getTransferDate(),
                transfer.getStatus(),
                transfer.getNotes(),
                items,
                transfer.getCreatedAt(),
                transfer.getUpdatedAt()
        );
    }
}