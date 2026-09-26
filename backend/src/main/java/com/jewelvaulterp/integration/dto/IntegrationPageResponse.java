package com.jewelvaulterp.integration.dto;

import java.util.List;

public record IntegrationPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
