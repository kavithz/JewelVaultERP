package com.jewelvaulterp.notification.controller;

import com.jewelvaulterp.notification.dto.CreateNotificationRequest;
import com.jewelvaulterp.notification.dto.NotificationResponse;
import com.jewelvaulterp.notification.entity.NotificationType;
import com.jewelvaulterp.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getAll() {
        return notificationService.getAll();
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable UUID id) {
        return notificationService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<NotificationResponse> getByCompany(@PathVariable UUID companyId) {
        return notificationService.getByCompany(companyId);
    }

    @GetMapping("/company/{companyId}/user/{userId}")
    public List<NotificationResponse> getByCompanyAndUser(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {
        return notificationService.getByCompanyAndUser(companyId, userId);
    }

    @GetMapping("/company/{companyId}/user/{userId}/unread")
    public List<NotificationResponse> getUnreadByCompanyAndUser(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {
        return notificationService.getUnreadByCompanyAndUser(companyId, userId);
    }

    @GetMapping("/company/{companyId}/unread")
    public List<NotificationResponse> getUnreadByCompany(@PathVariable UUID companyId) {
        return notificationService.getUnreadByCompany(companyId);
    }

    @GetMapping("/company/{companyId}/type/{notificationType}")
    public List<NotificationResponse> getByCompanyAndType(
            @PathVariable UUID companyId,
            @PathVariable NotificationType notificationType
    ) {
        return notificationService.getByCompanyAndType(companyId, notificationType);
    }

    @GetMapping("/company/{companyId}/user/{userId}/count-unread")
    public long countUnreadByCompanyAndUser(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {
        return notificationService.countUnreadByCompanyAndUser(companyId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        return notificationService.create(request);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable UUID id) {
        return notificationService.markAsRead(id);
    }

    @PatchMapping("/company/{companyId}/user/{userId}/read-all")
    public List<NotificationResponse> markAllUserNotificationsAsRead(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {
        return notificationService.markAllUserNotificationsAsRead(companyId, userId);
    }
}
