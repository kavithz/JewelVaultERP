package com.jewelvaulterp.warehouse.service;

import com.jewelvaulterp.auth.service.CurrentUserAccess;
import com.jewelvaulterp.branch.entity.Branch;
import com.jewelvaulterp.branch.repository.BranchRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.user.repository.UserRepository;
import com.jewelvaulterp.warehouse.dto.CreateWarehouseRequest;
import com.jewelvaulterp.warehouse.dto.WarehouseResponse;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private UserRepository userRepository;

    private CurrentUserAccess currentUserAccess;
    private WarehouseService warehouseService;
    private Company company;
    private Company otherCompany;
    private Branch branch;
    private Branch otherCompanyBranch;

    @BeforeEach
    void setUp() {
        currentUserAccess = new CurrentUserAccess(userRepository);
        warehouseService = new WarehouseService(warehouseRepository, branchRepository, currentUserAccess);

        company = new Company(UUID.randomUUID(), "Jewel Co", "Jewel Co Ltd", "US", "USD", LocalDateTime.now(), LocalDateTime.now());
        otherCompany = new Company(UUID.randomUUID(), "Other Co", "Other Co Ltd", "GB", "GBP", LocalDateTime.now(), LocalDateTime.now());

        branch = new Branch(UUID.randomUUID(), company, "Main Branch", "MAIN", "123 Main St", "New York", "US", "1234567890", "main@example.com", true, LocalDateTime.now(), LocalDateTime.now());
        otherCompanyBranch = new Branch(UUID.randomUUID(), otherCompany, "Other Branch", "OTHER", "456 Side St", "London", "GB", "0987654321", "other@example.com", true, LocalDateTime.now(), LocalDateTime.now());
        SecurityContextHolder.clearContext();
    }

    @Test
    void createWarehouseAllowsValidBranchFromSameCompany() {
        UUID companyId = company.getId();
        CreateWarehouseRequest request = new CreateWarehouseRequest(branch.getId(), "Stock Room", "SR-01", "Warehouse Address", "Back room");
        Warehouse savedWarehouse = new Warehouse(UUID.randomUUID(), branch, request.name(), request.code(), request.address(), request.description(), true, LocalDateTime.now(), LocalDateTime.now());

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString())
                .claim("companyId", companyId.toString())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                jwt,
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));

        when(branchRepository.findById(request.branchId())).thenReturn(Optional.of(branch));
        when(warehouseRepository.findByBranchIdAndCode(request.branchId(), request.code())).thenReturn(Optional.empty());
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(savedWarehouse);

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertEquals(branch.getId(), response.branchId());
        assertEquals(companyId, response.companyId());
        assertEquals(request.name(), response.name());
        assertEquals(request.code(), response.code());
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void createWarehouseRejectsBranchFromAnotherCompany() {
        CreateWarehouseRequest request = new CreateWarehouseRequest(otherCompanyBranch.getId(), "Stock Room", "SR-01", "Warehouse Address", "Back room");

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString())
                .claim("companyId", company.getId().toString())
                .build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                jwt,
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));

        when(branchRepository.findById(request.branchId())).thenReturn(Optional.of(otherCompanyBranch));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> warehouseService.createWarehouse(request)
        );

        assertNotNull(exception);
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }
}
