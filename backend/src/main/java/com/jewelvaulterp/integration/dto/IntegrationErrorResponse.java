package com.jewelvaulterp.integration.dto;

import java.time.LocalDateTime;

public record IntegrationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
