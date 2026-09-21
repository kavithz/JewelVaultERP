package com.jewelvaulterp.warehouse.service;

import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.branch.repository.BranchRepository;
import com.jewelvaulterp.warehouse.dto.CreateWarehouseRequest;
import com.jewelvaulterp.warehouse.dto.UpdateWarehouseRequest;
import com.jewelvaulterp.warehouse.dto.WarehouseResponse;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final BranchRepository branchRepository;

    public WarehouseService(
            WarehouseRepository warehouseRepository,
            BranchRepository branchRepository
    ) {
        this.warehouseRepository = warehouseRepository;
        this.branchRepository = branchRepository;
    }

    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WarehouseResponse createWarehouse(
            CreateWarehouseRequest request
    ) {
        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        if (warehouseRepository.findByBranchIdAndCode(
                request.branchId(),
                request.code()
        ).isPresent()) {
            throw new IllegalArgumentException("Warehouse code already exists");
        }

        LocalDateTime now = LocalDateTime.now();

        Warehouse warehouse = new Warehouse(
                UUID.randomUUID(),
                branch,
                request.name(),
                request.code(),
                request.address(),
                request.description(),
                true,
                now,
                now
        );

        return toResponse(warehouseRepository.save(warehouse));
    }

    public WarehouseResponse updateWarehouse(
            UUID id,
            UpdateWarehouseRequest request
    ) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));

        warehouseRepository.findByBranchIdAndCode(
                        warehouse.getBranch().getId(),
                        request.code()
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Warehouse code already exists");
                });

        warehouse.update(
                request.name(),
                request.code(),
                request.address(),
                request.description()
        );

        return toResponse(warehouseRepository.save(warehouse));
    }

    public WarehouseResponse updateStatus(
            UUID id,
            boolean active
    ) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));

        warehouse.setActive(active);

        return toResponse(warehouseRepository.save(warehouse));
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getBranch().getId(),
                warehouse.getBranch().getCompany().getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getAddress(),
                warehouse.getDescription(),
                warehouse.isActive(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }
}