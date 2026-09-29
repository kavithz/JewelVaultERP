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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@Mock private UserRepository userRepository;
	@Mock private RoleRepository roleRepository;
	@Mock private PermissionRepository permissionRepository;
	@Mock private PasswordEncoder passwordEncoder;
	@Mock private JwtEncoder jwtEncoder;

	private Company company;
	private User user;
	private Role companyRole;
	private Role foreignCompanyRole;
	private Role inactiveRole;
	private Permission permission;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		company = new Company(UUID.randomUUID(), "Jewel Co", "Jewel Co Ltd", "US", "USD", LocalDateTime.now(), LocalDateTime.now());
		user = new User(UUID.randomUUID(), company, "alice", "alice@example.test", "$2a$hash", true, LocalDateTime.now(), LocalDateTime.now());
		companyRole = new Role(UUID.randomUUID(), company, "ADMIN", null, true, LocalDateTime.now(), LocalDateTime.now());
		Company otherCompany = new Company(UUID.randomUUID(), "Other", "Other Ltd", "GB", "GBP", LocalDateTime.now(), LocalDateTime.now());
		foreignCompanyRole = new Role(UUID.randomUUID(), otherCompany, "OWNER", null, true, LocalDateTime.now(), LocalDateTime.now());
		inactiveRole = new Role(UUID.randomUUID(), company, "OLD_ROLE", null, false, LocalDateTime.now(), LocalDateTime.now());
		permission = new Permission(UUID.randomUUID(), "VIEW_REPORTS", "View reports");
		authService = new AuthService(userRepository, roleRepository, permissionRepository, passwordEncoder, jwtEncoder, "jewelvault-erp", 900);
	}

	@Test
	void loginReturnsTokenAndOnlySameCompanyActiveRolesAndPermissions() {
		when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("correct-password", user.getPasswordHash())).thenReturn(true);
		when(userRepository.findUserRoleIds(user.getId())).thenReturn(List.of(companyRole.getId(), foreignCompanyRole.getId(), inactiveRole.getId()));
		when(roleRepository.findAllById(any())).thenReturn(List.of(companyRole, foreignCompanyRole, inactiveRole));
		when(permissionRepository.findRolePermissions(companyRole.getId())).thenReturn(List.of(permission));
		when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
			Instant now = Instant.now();
			return Jwt.withTokenValue("signed-token")
					.header("alg", "HS256")
					.claim("sub", user.getId().toString())
					.claim("iss", "jewelvault-erp")
					.issuedAt(now)
					.expiresAt(now.plusSeconds(900))
					.build();
		});

		LoginResponse response = authService.login(new LoginRequest(" alice ", "correct-password"));

		assertEquals("signed-token", response.accessToken());
		assertEquals("Bearer", response.tokenType());
		assertEquals(900, response.expiresIn());
		assertEquals(user.getId(), response.user().id());
		assertEquals(company.getId(), response.user().companyId());
		assertEquals(List.of("ADMIN"), response.user().roles());
		assertEquals(List.of("VIEW_REPORTS"), response.user().permissions());

		ArgumentCaptor<JwtEncoderParameters> parameters = ArgumentCaptor.forClass(JwtEncoderParameters.class);
		verify(jwtEncoder).encode(parameters.capture());
		assertEquals(user.getId().toString(), parameters.getValue().getClaims().getSubject());
		assertEquals(List.of("ROLE_ADMIN", "VIEW_REPORTS"), parameters.getValue().getClaims().getClaim("authorities"));
		assertNull(parameters.getValue().getClaims().getClaim("password"));
		assertNull(parameters.getValue().getClaims().getClaim("passwordHash"));
		verify(userRepository).findByUsername("alice");
		verify(permissionRepository).findRolePermissions(companyRole.getId());
		verify(permissionRepository, never()).findRolePermissions(foreignCompanyRole.getId());
		verify(permissionRepository, never()).findRolePermissions(inactiveRole.getId());
	}

	@Test
	void loginRejectsUnknownUsername() {
		when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("missing", "password")));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		verifyNoInteractions(passwordEncoder, jwtEncoder);
	}

	@Test
	void loginRejectsIncorrectPassword() {
		when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-password", user.getPasswordHash())).thenReturn(false);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("alice", "wrong-password")));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		verifyNoInteractions(jwtEncoder);
	}

	@Test
	void loginRejectsInactiveUserBeforePasswordComparison() {
		user.setActive(false);
		when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("alice", "correct-password")));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		verifyNoInteractions(passwordEncoder, jwtEncoder);
	}
}
