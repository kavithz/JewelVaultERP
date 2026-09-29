package com.jewelvaulterp.auth.dto;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String username,
        String email,
        UUID companyId,
        List<String> roles,
        List<String> permissions
) {
}