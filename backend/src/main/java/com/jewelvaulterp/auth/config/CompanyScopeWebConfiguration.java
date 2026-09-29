package com.jewelvaulterp.auth.config;

import com.jewelvaulterp.auth.service.CurrentUserAccess;
import com.jewelvaulterp.auth.service.CompanyOwnershipResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

@Configuration
public class CompanyScopeWebConfiguration implements WebMvcConfigurer {

    private final CurrentUserAccess currentUserAccess;
    private final CompanyOwnershipResolver ownershipResolver;

    public CompanyScopeWebConfiguration(CurrentUserAccess currentUserAccess, CompanyOwnershipResolver ownershipResolver) {
        this.currentUserAccess = currentUserAccess;
        this.ownershipResolver = ownershipResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CompanyScopeInterceptor(currentUserAccess, ownershipResolver)).addPathPatterns("/api/**");
    }

    private static final class CompanyScopeInterceptor implements HandlerInterceptor {
        private final CurrentUserAccess currentUserAccess;
        private final CompanyOwnershipResolver ownershipResolver;

        private CompanyScopeInterceptor(CurrentUserAccess currentUserAccess, CompanyOwnershipResolver ownershipResolver) {
            this.currentUserAccess = currentUserAccess;
            this.ownershipResolver = ownershipResolver;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof JwtAuthenticationToken)) return true;

            Object variables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (variables instanceof Map<?, ?> pathVariables) {
                verify(pathVariables.get("companyId"));
                verifyUser(pathVariables.get("userId"), request.getRequestURI());
                verifyResourceId(pathVariables.get("id"), handler);
            }
            verify(request.getParameter("companyId"));
            verifyUser(request.getParameter("userId"), request.getRequestURI());
            return true;
        }

        private void verifyResourceId(Object rawId, Object handler) {
            if (rawId == null || !(handler instanceof org.springframework.web.method.HandlerMethod method)) return;
            try {
                UUID resourceId = rawId instanceof UUID id ? id : UUID.fromString(rawId.toString());
                if (!ownershipResolver.isControllerResourceOwnedBy(method, resourceId, currentUserAccess.companyId())) {
                    throw new AccessDeniedException("Access to this company's data is denied.");
                }
            } catch (IllegalArgumentException exception) {
                throw new AccessDeniedException("Access to this company's data is denied.");
            }
        }

        private void verify(Object rawCompanyId) {
            if (rawCompanyId == null) return;
            try {
                UUID requestedCompanyId = rawCompanyId instanceof UUID id
                        ? id
                        : UUID.fromString(rawCompanyId.toString());
                currentUserAccess.requireCompany(requestedCompanyId);
            } catch (IllegalArgumentException exception) {
                throw new AccessDeniedException("Access to this company is denied.");
            }
        }

        private void verifyUser(Object rawUserId, String requestUri) {
            if (rawUserId == null) return;
            try {
                UUID requestedUserId = rawUserId instanceof UUID id
                        ? id
                        : UUID.fromString(rawUserId.toString());
                if (requestUri.startsWith("/api/user-roles/")) {
                    currentUserAccess.requireUserInCompany(requestedUserId);
                } else {
                    currentUserAccess.requireUser(requestedUserId);
                }
            } catch (IllegalArgumentException exception) {
                throw new AccessDeniedException("Access to this user is denied.");
            }
        }
    }
}