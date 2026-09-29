package com.jewelvaulterp.auth.service;

import com.jewelvaulterp.auth.dto.LoginRequest;
import com.jewelvaulterp.auth.dto.LoginResponse;
import com.jewelvaulterp.permission.repository.PermissionRepository;
import com.jewelvaulterp.role.entity.Role;
import com.jewelvaulterp.role.repository.RoleRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationSeconds;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .filter(candidate -> candidate.isActive())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        List<Role> roles = userRepository.findUserRoleIds(user.getId()).stream()
                .map(roleRepository::findById)
                .flatMap(optionalRole -> optionalRole.stream())
                .filter(role -> role.isActive())
                .filter(role -> role.getCompany().getId().equals(user.getCompany().getId()))
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .toList();
        List<String> roleNames = roles.stream().map(role -> role.getName()).toList();
        List<String> permissions = roles.stream()
                .flatMap(role -> permissionRepository.findRolePermissions(role.getId()).stream())
                .map(permission -> permission.getName())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("userId", user.getId().toString())
                .claim("companyId", user.getCompany().getId().toString())
                .claim("roles", roleNames)
                .claim("permissions", permissions)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        LoginResponse.User responseUser = new LoginResponse.User(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getCompany().getId().toString(),
                roleNames,
                permissions
        );
        return new LoginResponse(token, "Bearer", expirationSeconds, responseUser);
    }
}