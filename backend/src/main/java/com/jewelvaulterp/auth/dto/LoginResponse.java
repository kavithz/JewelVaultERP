package com.jewelvaulterp.auth.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        User user
) {
    public record User(
            String id,
            String username,
            String email,
            String companyId,
            List<String> roles,
            List<String> permissions
    ) {
    }
}