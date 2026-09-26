package com.jewelvaulterp.integration.dto;

import java.time.LocalDateTime;

public record IntegrationHealthResponse(
        String service,
        String status,
        LocalDateTime timestamp
) {
}
