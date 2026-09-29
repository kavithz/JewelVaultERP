package com.jewelvaulterp.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthenticatedUserResponse user
) {
}