package com.jewelvaulterp.auth.service;

import com.jewelvaulterp.auth.dto.LoginRequest;
import com.jewelvaulterp.auth.dto.LoginResponse;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.permission.entity.Permission;
import com.jewelvaulterp.permission.repository.PermissionRepository;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PermissionRepository permissionRepository = mock(PermissionRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtEncoder jwtEncoder = mock(JwtEncoder.class);

    private AuthService authService;
    private User user;
    private UUID userId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        UUID companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Company company = new Company(companyId, "Test Co", "Test Co", "US", "USD", now, now);
        user = new User(userId, company, "admin", "admin@example.test", "encoded-password", true, now, now);
        Role role = new Role(roleId, company, "ADMIN", "Administrator", true, now, now);
        Permission permission = new Permission(UUID.randomUUID(), "USER_READ", "Read users");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(userRepository.findUserRoleIds(userId)).thenReturn(List.of(roleId));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findRolePermissions(roleId)).thenReturn(List.of(permission));
        when(passwordEncoder.matches("correct-password", "encoded-password")).thenReturn(true);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                Jwt.withTokenValue("signed.jwt.token")
                        .header("alg", "HS256")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(900))
                        .build());

        authService = new AuthService(
                userRepository,
                roleRepository,
                permissionRepository,
                passwordEncoder,
                jwtEncoder,
                "jewelvault-test",
                900
        );
    }

    @Test
    void loginReturnsJwtAndExistingCompanyRoleData() {
        LoginResponse response = authService.login(new LoginRequest(" admin ", "correct-password"));

        assertEquals("signed.jwt.token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900, response.expiresIn());
        assertEquals(userId.toString(), response.user().id());
        assertEquals(user.getCompany().getId().toString(), response.user().companyId());
        assertEquals(List.of("ADMIN"), response.user().roles());
        assertEquals(List.of("USER_READ"), response.user().permissions());
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void loginRejectsInvalidPassword() {
        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("admin", "wrong-password")));
    }
}