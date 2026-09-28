package com.jewelvaulterp.auth.config;

import com.jewelvaulterp.auth.dto.LoginRequest;
import com.jewelvaulterp.auth.controller.AuthController;
import com.jewelvaulterp.auth.service.AuthService;
import com.jewelvaulterp.auth.service.CompanyOwnershipResolver;
import com.jewelvaulterp.common.config.SecurityConfig;
import com.jewelvaulterp.role.controller.RoleController;
import com.jewelvaulterp.role.dto.CreateRoleRequest;
import com.jewelvaulterp.role.dto.RoleResponse;
import com.jewelvaulterp.role.service.RoleService;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import tools.jackson.databind.ObjectMapper;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AuthController.class, ProtectedProbeController.class, RoleController.class})
@Import({
        SecurityConfig.class,
        JwtConfig.class,
        CompanyScopeWebConfiguration.class,
        CompanyScopeRequestBodyAdvice.class,
        com.jewelvaulterp.auth.service.CurrentUserAccess.class
})
class AuthSecurityIntegrationTest {

        private static final String TEST_SIGNING_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
        private static final String TEST_COMPANY_ID = "00000000-0000-0000-0000-000000000001";
        private static final String TEST_USER_ID = "00000000-0000-0000-0000-000000000003";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtEncoder jwtEncoder;
    @Autowired private JwtAuthenticationConverter jwtAuthenticationConverter;
        @MockitoBean private AuthService authService;
        @MockitoBean private UserRepository userRepository;
        @MockitoBean private CompanyOwnershipResolver companyOwnershipResolver;
        @MockitoBean private RoleService roleService;

        @BeforeEach
        void setUp() {
                Company company = new Company(UUID.fromString(TEST_COMPANY_ID), "Test", "Test Ltd", "US", "USD", java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
                User activeUser = new User(UUID.fromString(TEST_USER_ID), company, "test-user", "test@example.test", "hash", true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
                when(userRepository.findByIdAndCompanyId(UUID.fromString(TEST_USER_ID), UUID.fromString(TEST_COMPANY_ID)))
                                .thenReturn(Optional.of(activeUser));
                when(companyOwnershipResolver.belongsToCompany(any(), any(UUID.class))).thenReturn(true);
        }

    @Test
    void loginEndpointIsPublicAndValidatesItsRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRejectsInvalidCredentialsWithUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Invalid username or password."
                )
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("alice", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid username or password."));
    }

    @Test
    void protectedApiRejectsMissingBearerToken() throws Exception {
        mockMvc.perform(get("/api/integration/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedApiRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/integration/health").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedApiRejectsExpiredBearerToken() throws Exception {
        mockMvc.perform(get("/api/security-probe").header("Authorization", "Bearer " + expiredToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedApiAcceptsValidBearerToken() throws Exception {
        mockMvc.perform(get("/api/security-probe").header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600))))
            .andExpect(status().isOk());
    }

    @Test
    void protectedApiRejectsTokenAfterUserIsDeactivated() throws Exception {
        Company company = new Company(UUID.fromString(TEST_COMPANY_ID), "Test", "Test Ltd", "US", "USD", java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        User inactiveUser = new User(UUID.fromString(TEST_USER_ID), company, "test-user", "test@example.test", "hash", false, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        when(userRepository.findByIdAndCompanyId(UUID.fromString(TEST_USER_ID), UUID.fromString(TEST_COMPANY_ID)))
                .thenReturn(Optional.of(inactiveUser));

        mockMvc.perform(get("/api/security-probe").header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userIdParameterCannotImpersonateAnotherUser() throws Exception {
        mockMvc.perform(get("/api/user-scope-probe/" + TEST_USER_ID)
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/user-scope-probe/00000000-0000-0000-0000-000000000004")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminOnlyOperationRejectsNormalUserAndAllowsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin-probe")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600), List.of())))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin-probe")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600), List.of("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void roleCreationEndpointRequiresExistingAdminAuthority() throws Exception {
        UUID roleId = UUID.fromString("00000000-0000-0000-0000-000000000005");
        UUID companyId = UUID.fromString(TEST_COMPANY_ID);
        when(roleService.create(any(CreateRoleRequest.class))).thenReturn(
                new RoleResponse(roleId, companyId, "ADMIN", null, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
        );
        String request = objectMapper.writeValueAsString(new CreateRoleRequest(companyId, "ADMIN", null));

        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600), List.of("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600), List.of("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }

    @Test
    void jwtRoleAndPermissionClaimsBecomeAuthorities() {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(TEST_USER_ID)
                .claim("companyId", TEST_COMPANY_ID)
                .claim("authorities", List.of("ROLE_ADMIN", "VIEW_REPORTS"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(600))
                .build();

        Authentication authentication = jwtAuthenticationConverter.convert(jwt);

        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("VIEW_REPORTS")));
    }

    @Test
    void permissionProtectedMethodRequiresItsAuthority() throws Exception {
        mockMvc.perform(get("/api/permission-probe")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600), List.of())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/permission-probe")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600), List.of("VIEW_REPORTS"))))
                .andExpect(status().isOk());
    }

    @Test
    void companyPathMustMatchTheTokenCompany() throws Exception {
        mockMvc.perform(get("/api/company-scope-probe/" + TEST_COMPANY_ID)
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/company-scope-probe/00000000-0000-0000-0000-000000000002")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600))))
                .andExpect(status().isForbidden());
    }

    @Test
    void companyIdInCreateBodyMustMatchTheTokenCompany() throws Exception {
        mockMvc.perform(post("/api/company-body-probe")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CompanyProbeRequest(UUID.fromString(TEST_COMPANY_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/company-body-probe")
                        .header("Authorization", "Bearer " + token(Instant.now().plusSeconds(600)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CompanyProbeRequest(UUID.fromString("00000000-0000-0000-0000-000000000002")))))
                .andExpect(status().isForbidden());
    }

    private String token(Instant expiresAt) {
        return token(expiresAt, List.of("ROLE_ADMIN", "VIEW_REPORTS"));
    }

    private String token(Instant expiresAt, List<String> authorities) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(TEST_USER_ID)
                .issuer("jewelvault-erp")
                .claim("companyId", TEST_COMPANY_ID)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("authorities", authorities)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims
        )).getTokenValue();
    }

    private String expiredToken() throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(TEST_USER_ID)
                .issuer("jewelvault-erp")
                .claim("companyId", TEST_COMPANY_ID)
                .issueTime(Date.from(now.minusSeconds(120)))
                .expirationTime(Date.from(now.minusSeconds(60)))
                .claim("authorities", List.of("ROLE_ADMIN", "VIEW_REPORTS"))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(Base64.getDecoder().decode(TEST_SIGNING_KEY)));
        return jwt.serialize();
    }

        record CompanyProbeRequest(UUID companyId) {}
}