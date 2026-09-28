package com.jewelvaulterp.auth.config;

import com.jewelvaulterp.auth.service.CurrentUserAccess;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.UUID;

@ControllerAdvice
public class CompanyScopeRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final CurrentUserAccess currentUserAccess;

    public CompanyScopeRequestBodyAdvice(CurrentUserAccess currentUserAccess) {
        this.currentUserAccess = currentUserAccess;
    }

    @Override
    public boolean supports(
            MethodParameter methodParameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object afterBodyRead(
            Object body,
            HttpInputMessage inputMessage,
            MethodParameter parameter,
            Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        HttpServletRequest request = currentRequest();
        if (request == null || !request.getRequestURI().startsWith("/api/")
                || "/api/auth/login".equals(request.getRequestURI())) {
            return body;
        }

        UUID companyId = readCompanyId(body);
        if (companyId != null) currentUserAccess.requireCompany(companyId);
        return body;
    }

    private UUID readCompanyId(Object body) {
        Method accessor = findAccessor(body.getClass(), "companyId", "getCompanyId");
        if (accessor == null) return null;
        try {
            Object value = accessor.invoke(body);
            return value instanceof UUID id ? id : value == null ? null : UUID.fromString(value.toString());
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            throw new org.springframework.security.access.AccessDeniedException("Access to this company is denied.");
        }
    }

    private Method findAccessor(Class<?> type, String... names) {
        for (String name : names) {
            try {
                return type.getMethod(name);
            } catch (NoSuchMethodException ignored) {
                // Try the next conventional accessor name.
            }
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}