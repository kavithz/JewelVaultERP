package com.jewelvaulterp.auth.service;

import com.jewelvaulterp.auth.dto.AuthenticatedUserResponse;
import com.jewelvaulterp.auth.dto.LoginRequest;
import com.jewelvaulterp.auth.dto.LoginResponse;
import com.jewelvaulterp.permission.repository.PermissionRepository;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long tokenTtlSeconds;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer:jewelvault-erp}") String issuer,
            @Value("${security.jwt.ttl-seconds:900}") long tokenTtlSeconds
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        if (tokenTtlSeconds < 1) {
            throw new IllegalArgumentException("JWT token lifetime must be positive.");
        }
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(this::invalidCredentials);
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        UUID companyId = user.getCompany().getId();
        List<UUID> roleIds = userRepository.findUserRoleIds(user.getId());
        List<Role> roles = roleIds == null || roleIds.isEmpty()
                ? List.of()
                : roleRepository.findAllById(roleIds).stream()
                        .filter(Role::isActive)
                        .filter(role -> companyId.equals(role.getCompany().getId()))
                        .sorted(Comparator.comparing(Role::getName))
                        .toList();

        List<String> roleNames = roles.stream().map(Role::getName).toList();
        List<String> permissions = roles.stream()
                .flatMap(role -> permissionRepository.findRolePermissions(role.getId()).stream())
                .map(permission -> permission.getName())
                .distinct()
                .sorted()
                .toList();
        List<String> authorities = new ArrayList<>();
        roleNames.stream().map(name -> "ROLE_" + name).forEach(authorities::add);
        authorities.addAll(permissions);

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(tokenTtlSeconds);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("username", user.getUsername())
                .claim("companyId", companyId.toString())
                .claim("roles", roleNames)
                .claim("permissions", permissions)
                .claim("authorities", authorities)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();

        AuthenticatedUserResponse authenticatedUser = new AuthenticatedUserResponse(
                user.getId(), user.getUsername(), user.getEmail(), companyId, roleNames, permissions
        );
        return new LoginResponse(token, "Bearer", tokenTtlSeconds, authenticatedUser);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }
}