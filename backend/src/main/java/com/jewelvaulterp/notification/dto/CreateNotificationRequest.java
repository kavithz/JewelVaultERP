package com.jewelvaulterp.notification.dto;

import com.jewelvaulterp.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull UUID companyId,
        UUID userId,
        @NotNull NotificationType notificationType,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 1000) String message,
        @Size(max = 100) String referenceType,
        UUID referenceId
) {
}
