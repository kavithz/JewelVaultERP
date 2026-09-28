package com.jewelvaulterp.auth.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import com.jewelvaulterp.user.repository.UserRepository;

import java.util.UUID;

@Component("currentUserAccess")
public class CurrentUserAccess {

    private final UserRepository userRepository;

    public CurrentUserAccess(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID userId() {
        return uuidClaim("sub");
    }

    public UUID companyId() {
        return uuidClaim("companyId");
    }

    public UUID requireCompany(UUID requestedCompanyId) {
        if (requestedCompanyId == null || !companyId().equals(requestedCompanyId)) {
            throw new AccessDeniedException("Access to this company is denied.");
        }
        return requestedCompanyId;
    }

    public UUID requireUser(UUID requestedUserId) {
        if (requestedUserId == null || !userId().equals(requestedUserId)) {
            throw new AccessDeniedException("Access to this user is denied.");
        }
        return requestedUserId;
    }

    public UUID requireUserAccess(UUID requestedUserId) {
        if (hasAdminAuthority()) return requireUserInCompany(requestedUserId);
        return requireUser(requestedUserId);
    }

    public boolean hasAdminAuthority() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    public UUID requireUserInCompany(UUID requestedUserId) {
        UUID callerCompanyId = companyId();
        boolean belongsToCompany = userRepository.findByIdAndCompanyId(requestedUserId, callerCompanyId).isPresent();
        if (!belongsToCompany) {
            throw new AccessDeniedException("Access to this user is denied.");
        }
        return requestedUserId;
    }

    private UUID uuidClaim(String claim) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Authenticated user context is unavailable.");
        }

        String value = claim.equals("sub") ? jwt.getSubject() : jwt.getClaimAsString(claim);
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new AccessDeniedException("Authenticated user context is invalid.");
        }
    }
}