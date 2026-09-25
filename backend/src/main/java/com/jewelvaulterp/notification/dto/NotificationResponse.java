package com.jewelvaulterp.notification.dto;

import com.jewelvaulterp.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID companyId,
        UUID userId,
        NotificationType notificationType,
        String title,
        String message,
        String referenceType,
        UUID referenceId,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}
