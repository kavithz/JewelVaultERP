package com.jewelvaulterp.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

@Component
public class TenantAccessInterceptor implements HandlerInterceptor {

    private final TenantAccessService tenantAccessService;

    public TenantAccessInterceptor(TenantAccessService tenantAccessService) {
        this.tenantAccessService = tenantAccessService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken)) {
            return true;
        }
        String requestedCompanyId = request.getParameter("companyId");
        if (requestedCompanyId != null) {
            tenantAccessService.requireCompany(requestedCompanyId);
        }
        Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variables instanceof Map<?, ?> pathVariables) {
            for (Map.Entry<?, ?> entry : pathVariables.entrySet()) {
                String variableName = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                if ("companyId".equals(variableName)) {
                    tenantAccessService.requireCompany(value);
                    continue;
                }
                tenantAccessService.entityTypeFor(variableName, handlerMethod.getBeanType())
                        .ifPresent(entityType -> parseUuid(value).ifPresent(id ->
                                tenantAccessService.requireEntityCompany(entityType, id)));
            }
        }
        return true;
    }

    private java.util.Optional<UUID> parseUuid(String value) {
        try {
            return java.util.Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }
}