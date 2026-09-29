package com.jewelvaulterp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductionHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void actuatorHealthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void corsPreflightAllowsConfiguredOrigins() throws Exception {
        mockMvc.perform(options("/api/branches")
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void validationErrorsReturnConsistentJsonResponse() throws Exception {
        String invalidRequest = "{\"companyId\":null,\"username\":\"\",\"email\":\"bad-email\",\"password\":\"\"}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/users"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void malformedJsonReturnsSafeGenericError() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-valid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body or invalid request data."));
    }

    @Test
    void protectedApiRejectsRequestsWithoutBearerAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidLoginReturnsUnauthorizedInsteadOfInternalServerError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"missing-user\",\"password\":\"invalid\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password."));
    }

    @Test
    void expiredJwtCannotAccessProtectedApi() throws Exception {
        Instant expiredAt = Instant.now().minusSeconds(60);
        String expiredToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                JwtClaimsSet.builder()
                        .issuer("jewelvault-erp")
                        .subject("expired-user")
                        .issuedAt(expiredAt.minusSeconds(60))
                        .expiresAt(expiredAt)
                        .claim("companyId", UUID.randomUUID().toString())
                        .claim("roles", List.of("ADMIN"))
                        .claim("permissions", List.of())
                        .build()
        )).getTokenValue();

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void auditorCannotMutateApiResources() throws Exception {
        mockMvc.perform(post("/api/roles")
                        .with(jwt().jwt(token -> token.claim("companyId", UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_AUDITOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestForAnotherCompanyIsDenied() throws Exception {
        UUID tokenCompanyId = UUID.randomUUID();
        UUID requestedCompanyId = UUID.randomUUID();

        mockMvc.perform(get("/api/dashboard/{companyId}", requestedCompanyId)
                        .with(jwt().jwt(token -> token.claim("companyId", tokenCompanyId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRequestCannotSpecifyAnotherCompany() throws Exception {
        UUID tokenCompanyId = UUID.randomUUID();
        UUID requestCompanyId = UUID.randomUUID();

        mockMvc.perform(post("/api/customers")
                        .with(jwt().jwt(token -> token.claim("companyId", tokenCompanyId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyId\":\"" + requestCompanyId + "\",\"name\":\"Tenant test\",\"code\":\"TENANT-TEST\"}"))
                .andExpect(status().isForbidden());
    }
}
