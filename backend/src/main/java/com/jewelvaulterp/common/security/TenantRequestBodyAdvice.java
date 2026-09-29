package com.jewelvaulterp.common.security;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Method;
import java.util.UUID;

@ControllerAdvice(basePackages = "com.jewelvaulterp")
public class TenantRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final TenantAccessService tenantAccessService;

    public TenantRequestBodyAdvice(TenantAccessService tenantAccessService) {
        this.tenantAccessService = tenantAccessService;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, java.lang.reflect.Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                java.lang.reflect.Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            readCompanyId(body).ifPresent(tenantAccessService::requireCompany);
        }
        return body;
    }

    private java.util.Optional<UUID> readCompanyId(Object body) {
        for (String methodName : new String[]{"getCompanyId", "companyId"}) {
            try {
                Method method = body.getClass().getMethod(methodName);
                Object value = method.invoke(body);
                if (value instanceof UUID id) {
                    return java.util.Optional.of(id);
                }
                if (value instanceof String id) {
                    return java.util.Optional.of(UUID.fromString(id));
                }
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                continue;
            }
        }
        return java.util.Optional.empty();
    }
}