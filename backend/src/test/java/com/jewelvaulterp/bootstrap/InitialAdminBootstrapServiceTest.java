package com.jewelvaulterp.bootstrap;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.role.service.RoleService;
import com.jewelvaulterp.role.service.UserRoleService;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import com.jewelvaulterp.user.service.UserService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialAdminBootstrapServiceTest {

    private static final String ADMIN_PASSWORD = "unit-test-only-password";

    @Mock private CompanyRepository companyRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final AtomicReference<Company> savedCompany = new AtomicReference<>();
    private final AtomicReference<Role> savedRole = new AtomicReference<>();
    private final AtomicReference<User> savedUser = new AtomicReference<>();
    private InitialAdminBootstrapService bootstrapService;

    @BeforeEach
    void setUp() {
        RoleService roleService = new RoleService(roleRepository, companyRepository, userRepository);
        UserService userService = new UserService(userRepository, companyRepository, passwordEncoder);
        UserRoleService userRoleService = new UserRoleService(userRepository, roleRepository);
        bootstrapService = new InitialAdminBootstrapService(
                companyRepository,
                userRepository,
                roleRepository,
                roleService,
                userService,
                userRoleService,
                validator
        );
    }

    private void stubEmptyDatabaseForBootstrap() {
        when(userRepository.existsActiveAdmin()).thenReturn(false);
        when(companyRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(roleRepository.count()).thenReturn(0L);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            savedCompany.set(company);
            return company;
        });
        when(companyRepository.findById(any(UUID.class))).thenAnswer(invocation ->
                Optional.ofNullable(savedCompany.get())
                        .filter(company -> company.getId().equals(invocation.getArgument(0))));
        when(roleRepository.findByCompanyIdAndNameIgnoreCase(any(UUID.class), anyString()))
                .thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            savedRole.set(role);
            return role;
        });
        when(roleRepository.findByIdAndCompanyId(any(UUID.class), any(UUID.class))).thenAnswer(invocation ->
                Optional.ofNullable(savedRole.get())
                        .filter(role -> role.getId().equals(invocation.getArgument(0)))
                        .filter(role -> role.getCompany().getId().equals(invocation.getArgument(1))));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            savedUser.set(user);
            return user;
        });
        when(userRepository.findById(any(UUID.class))).thenAnswer(invocation ->
                Optional.ofNullable(savedUser.get())
                        .filter(user -> user.getId().equals(invocation.getArgument(0))));
        when(userRepository.findUserRoleIds(any(UUID.class))).thenReturn(List.of());
    }

    @Test
    void emptyDatabaseCreatesCompanyAdminRoleAndBcryptAdminUser() {
        stubEmptyDatabaseForBootstrap();

        InitialAdminBootstrapService.BootstrapResult result = bootstrapService.createInitialAdmin(
                "admin", "admin@example.test", ADMIN_PASSWORD
        );

        assertEquals("JewelVaultERP", savedCompany.get().getName());
        assertEquals(null, savedCompany.get().getLegalName());
        assertEquals("LK", savedCompany.get().getCountryCode());
        assertEquals("LKR", savedCompany.get().getCurrencyCode());
        assertEquals(savedCompany.get().getId(), savedRole.get().getCompany().getId());
        assertEquals("ADMIN", savedRole.get().getName());
        assertEquals(savedCompany.get().getId(), savedUser.get().getCompany().getId());
        assertEquals("admin", savedUser.get().getUsername());
        assertNotEquals(ADMIN_PASSWORD, savedUser.get().getPasswordHash());
        assertTrue(passwordEncoder.matches(ADMIN_PASSWORD, savedUser.get().getPasswordHash()));
        assertEquals(savedCompany.get().getId(), result.companyId());
        assertEquals(savedRole.get().getId(), result.adminRoleId());
        assertEquals(savedUser.get().getId(), result.adminUserId());
        verify(userRepository).assignRoleToUser(savedUser.get().getId(), savedRole.get().getId());
    }

    @Test
    void rerunRefusesWithoutCreatingDuplicates() {
        stubEmptyDatabaseForBootstrap();
        AtomicInteger activeAdminChecks = new AtomicInteger();
        when(userRepository.existsActiveAdmin()).thenAnswer(invocation -> activeAdminChecks.getAndIncrement() > 0);

        bootstrapService.createInitialAdmin("admin", "admin@example.test", ADMIN_PASSWORD);

        assertThrows(IllegalStateException.class, () -> bootstrapService.createInitialAdmin(
                "admin", "admin@example.test", ADMIN_PASSWORD
        ));

        verify(companyRepository).save(any(Company.class));
        verify(roleRepository).save(any(Role.class));
        verify(userRepository).save(any(User.class));
        verify(userRepository).assignRoleToUser(savedUser.get().getId(), savedRole.get().getId());
    }

    @Test
    void existingActiveAdminRefusesBeforeAnyWrites() {
        when(userRepository.existsActiveAdmin()).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> bootstrapService.createInitialAdmin(
                "admin", "admin@example.test", ADMIN_PASSWORD
        ));

        verify(companyRepository, never()).save(any(Company.class));
        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void existingCompanyDataRefusesBeforeAnyWrites() {
        when(userRepository.existsActiveAdmin()).thenReturn(false);
        when(companyRepository.count()).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> bootstrapService.createInitialAdmin(
                "admin", "admin@example.test", ADMIN_PASSWORD
        ));

        verify(companyRepository, never()).save(any(Company.class));
        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, never()).save(any(User.class));
    }
}