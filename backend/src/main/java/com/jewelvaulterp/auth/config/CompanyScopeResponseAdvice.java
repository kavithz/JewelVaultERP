package com.jewelvaulterp.auth.config;

import com.jewelvaulterp.auth.service.CompanyOwnershipResolver;
import com.jewelvaulterp.common.error.ApiErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ControllerAdvice
public class CompanyScopeResponseAdvice implements ResponseBodyAdvice<Object> {

    private final CompanyOwnershipResolver ownershipResolver;

    public CompanyScopeResponseAdvice(CompanyOwnershipResolver ownershipResolver) {
        this.ownershipResolver = ownershipResolver;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body == null || body instanceof ApiErrorResponse || !request.getURI().getPath().startsWith("/api/")) {
            return body;
        }
        UUID companyId = authenticatedCompanyId();
        if (companyId == null) return body;

        if (body instanceof Collection<?> records) {
            return records.stream()
                    .filter(record -> belongsToCompanyOrGlobal(record, companyId))
                    .toList();
        }
        if (!ownershipResolver.belongsToCompany(body, companyId)) {
            throw new AccessDeniedException("Access to this company's data is denied.");
        }
        return body;
    }

    private boolean belongsToCompanyOrGlobal(Object record, UUID companyId) {
        return ownershipResolver.belongsToCompany(record, companyId);
    }

    private UUID authenticatedCompanyId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) return null;
        try {
            return UUID.fromString(jwt.getClaimAsString("companyId"));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new AccessDeniedException("Authenticated company context is invalid.");
        }
    }
}