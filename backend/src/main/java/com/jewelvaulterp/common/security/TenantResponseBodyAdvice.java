package com.jewelvaulterp.common.security;

import com.jewelvaulterp.common.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;

@ControllerAdvice(basePackages = "com.jewelvaulterp")
public class TenantResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final TenantAccessService tenantAccessService;
    private final HttpServletRequest servletRequest;

    public TenantResponseBodyAdvice(TenantAccessService tenantAccessService, HttpServletRequest servletRequest) {
        this.tenantAccessService = tenantAccessService;
        this.servletRequest = servletRequest;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getContainingClass().getPackageName().startsWith("com.jewelvaulterp.");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken) || body == null) {
            return body;
        }
        if (body instanceof ApiErrorResponse) {
            return body;
        }
        if (hasExplicitCompanyScope()) {
            return body;
        }
        if (body instanceof Collection<?> items) {
            return items.stream()
                    .filter(item -> tenantAccessService.belongsToCurrentCompany(item)
                            || tenantAccessService.isSharedCatalogItem(item))
                    .toList();
        }
        if (tenantAccessService.isSharedCatalogItem(body)) {
            return body;
        }
        tenantAccessService.companyIdOf(body)
                .ifPresentOrElse(tenantAccessService::requireCompany,
                        () -> { throw new AccessDeniedException("Tenant scope could not be verified."); });
        return body;
    }

    private boolean hasExplicitCompanyScope() {
        String path = servletRequest.getRequestURI();
        return path.equals("/api/integration/health")
            || servletRequest.getParameter("companyId") != null
                || path.matches(".*/company/[^/]+(?:/.*)?")
                || path.matches("/api/dashboard/[^/]+(?:/.*)?")
                || path.matches("/api/(?:financial/reports|accounting/reports)/[^/]+/[^/]+")
                || path.matches("/api/integration/[^/]+(?:/.*)?");
    }
}