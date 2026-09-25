package com.jewelvaulterp.role;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.permission.dto.AssignPermissionRequest;
import com.jewelvaulterp.permission.dto.PermissionResponse;
import com.jewelvaulterp.permission.entity.Permission;
import com.jewelvaulterp.permission.repository.PermissionRepository;
import com.jewelvaulterp.permission.service.PermissionService;
import com.jewelvaulterp.role.dto.CreateRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.dto.UpdateRoleRequest;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.role.service.RoleService;
import com.jewelvaulterp.role.service.UserRoleService;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    @InjectMocks
    private PermissionService permissionService;

    @InjectMocks
    private UserRoleService userRoleService;

    private Company companyA;
    private Company companyB;
    private User userA;
    private User userB;
    private Role roleA;
    private Role roleB;
    private Permission permission;

    @BeforeEach
    void setUp() {
        companyA = new Company(UUID.randomUUID(), "Alpha", "Alpha Co", "US", "USD", LocalDateTime.now(), LocalDateTime.now());
        companyB = new Company(UUID.randomUUID(), "Beta", "Beta Co", "GB", "GBP", LocalDateTime.now(), LocalDateTime.now());

        userA = new User(UUID.randomUUID(), companyA, "alice", "alice@alpha.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
        userB = new User(UUID.randomUUID(), companyB, "bob", "bob@beta.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

        roleA = new Role(UUID.randomUUID(), companyA, "ADMIN", "Admin users", true, LocalDateTime.now(), LocalDateTime.now());
        roleB = new Role(UUID.randomUUID(), companyB, "MANAGER", "Managers", true, LocalDateTime.now(), LocalDateTime.now());
        permission = new Permission(UUID.randomUUID(), "VIEW_REPORTS", "Can view reports");
    }

    @Test
    void createRole_shouldPersistNewRole() {
        when(companyRepository.findById(companyA.getId())).thenReturn(Optional.of(companyA));
        when(roleRepository.findByCompanyIdAndNameIgnoreCase(companyA.getId(), "ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = roleService.create(new CreateRoleRequest(companyA.getId(), "ADMIN", "Admin users"));

        assertNotNull(response);
        assertEquals(companyA.getId(), response.companyId());
        assertEquals("ADMIN", response.name());
        assertTrue(response.active());
    }

    @Test
    void createRole_shouldRejectDuplicateRoleNameInSameCompany() {
        when(companyRepository.findById(companyA.getId())).thenReturn(Optional.of(companyA));
        when(roleRepository.findByCompanyIdAndNameIgnoreCase(companyA.getId(), "ADMIN")).thenReturn(Optional.of(roleA));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> roleService.create(new CreateRoleRequest(companyA.getId(), "ADMIN", "Duplicate")));

        assertTrue(ex.getMessage().contains("duplicate"));
    }

    @Test
    void getByCompany_shouldOnlyReturnCompanyRoles() {
        when(companyRepository.findById(companyA.getId())).thenReturn(Optional.of(companyA));
        when(roleRepository.findByCompanyIdOrderByNameAsc(companyA.getId())).thenReturn(List.of(roleA));

        List<RoleResponse> responses = roleService.getByCompany(companyA.getId());

        assertEquals(1, responses.size());
        assertEquals(companyA.getId(), responses.get(0).companyId());
        assertEquals("ADMIN", responses.get(0).name());
    }

    @Test
    void updateRole_shouldUpdateRoleFields() {
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(roleRepository.findByCompanyIdAndNameIgnoreCase(companyA.getId(), "SUPER_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = roleService.update(roleA.getId(), companyA.getId(),
                new UpdateRoleRequest("SUPER_ADMIN", "Super admin role", true));

        assertEquals("SUPER_ADMIN", response.name());
        assertEquals("Super admin role", response.description());
        assertTrue(response.active());
    }

    @Test
    void activateAndDeactivate_shouldToggleActiveFlag() {
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse activated = roleService.activate(roleA.getId(), companyA.getId());
        assertTrue(activated.active());

        RoleResponse deactivated = roleService.deactivate(roleA.getId(), companyA.getId());
        assertFalse(deactivated.active());
    }

    @Test
    void deleteRole_shouldAllowWhenNotAssigned() {
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(userRepository.findByCompanyId(companyA.getId())).thenReturn(List.of(userA));
        when(userRepository.findUserRoleIds(userA.getId())).thenReturn(List.of());

        assertDoesNotThrow(() -> roleService.delete(roleA.getId(), companyA.getId()));
    }

    @Test
    void deleteRole_shouldRejectWhenAssignedToUser() {
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(userRepository.findByCompanyId(companyA.getId())).thenReturn(List.of(userA));
        when(userRepository.findUserRoleIds(userA.getId())).thenReturn(List.of(roleA.getId()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> roleService.delete(roleA.getId(), companyA.getId()));

        assertTrue(ex.getMessage().contains("assigned"));
    }

    @Test
    void assignPermission_shouldPersistRolePermission() {
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(permissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));
        when(permissionRepository.findRolePermissions(roleA.getId())).thenReturn(List.of());

        PermissionResponse response = permissionService.assignToRole(roleA.getId(), companyA.getId(),
                new AssignPermissionRequest(permission.getId()));

        assertNotNull(response);
        assertEquals(permission.getId(), response.id());
    }

    @Test
    void assignPermission_shouldRejectDuplicateAssignment() {
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(permissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));
        when(permissionRepository.findRolePermissions(roleA.getId())).thenReturn(List.of(permission));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> permissionService.assignToRole(roleA.getId(), companyA.getId(),
                        new AssignPermissionRequest(permission.getId())));

        assertTrue(ex.getMessage().contains("already"));
    }

    @Test
    void removePermission_shouldRemoveAssociation() {
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(permissionRepository.findById(permission.getId())).thenReturn(Optional.of(permission));
        when(permissionRepository.findRolePermissions(roleA.getId())).thenReturn(List.of(permission));

        assertDoesNotThrow(() -> permissionService.removeFromRole(roleA.getId(), permission.getId(), companyA.getId()));
    }

    @Test
    void assignRoleToUser_shouldPersistAssignment() {
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(userRepository.findUserRoleIds(userA.getId())).thenReturn(List.of());

        RoleResponse response = userRoleService.assignRole(userA.getId(), new com.jewelvaulterp.role.dto.AssignRoleRequest(roleA.getId()));

        assertNotNull(response);
        assertEquals(roleA.getId(), response.id());
    }

    @Test
    void assignRoleToUser_shouldRejectCrossCompanyRole() {
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(roleRepository.findByIdAndCompanyId(roleB.getId(), companyA.getId())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userRoleService.assignRole(userA.getId(), new com.jewelvaulterp.role.dto.AssignRoleRequest(roleB.getId())));

        assertTrue(ex.getMessage().contains("company"));
    }

    @Test
    void assignRoleToUser_shouldRejectDuplicateAssignment() {
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(userRepository.findUserRoleIds(userA.getId())).thenReturn(List.of(roleA.getId()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userRoleService.assignRole(userA.getId(), new com.jewelvaulterp.role.dto.AssignRoleRequest(roleA.getId())));

        assertTrue(ex.getMessage().contains("already"));
    }

    @Test
    void removeRoleFromUser_shouldRemoveAssignment() {
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(roleRepository.findByIdAndCompanyId(roleA.getId(), companyA.getId())).thenReturn(Optional.of(roleA));
        when(userRepository.findUserRoleIds(userA.getId())).thenReturn(List.of(roleA.getId()));

        assertDoesNotThrow(() -> userRoleService.removeRole(userA.getId(), roleA.getId()));
    }
}
