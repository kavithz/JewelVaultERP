package com.jewelvaulterp.notification.service;

import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.notification.dto.CreateNotificationRequest;
import com.jewelvaulterp.notification.dto.NotificationResponse;
import com.jewelvaulterp.notification.entity.Notification;
import com.jewelvaulterp.notification.entity.NotificationType;
import com.jewelvaulterp.notification.repository.NotificationRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationResponse create(CreateNotificationRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!company.getId().equals(user.getCompany().getId())) {
                throw new IllegalArgumentException("User does not belong to the supplied company");
            }
        }

        Notification notification = new Notification(
                UUID.randomUUID(),
                company,
                user,
                request.notificationType(),
                request.title(),
                request.message(),
                request.referenceType(),
                request.referenceId(),
                false,
                LocalDateTime.now(),
                null
        );

        return toResponse(notificationRepository.save(notification));
    }

    public NotificationResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    public List<NotificationResponse> getByCompany(UUID companyId) {
        validateCompanyExists(companyId);
        return notificationRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getByCompanyAndUser(UUID companyId, UUID userId) {
        validateCompanyExists(companyId);
        User user = validateUserInCompany(companyId, userId);
        return notificationRepository.findByCompanyIdAndUserIdOrderByCreatedAtDesc(companyId, user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadByCompanyAndUser(UUID companyId, UUID userId) {
        validateCompanyExists(companyId);
        User user = validateUserInCompany(companyId, userId);
        return notificationRepository.findByCompanyIdAndUserIdAndReadFalseOrderByCreatedAtDesc(companyId, user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadByCompany(UUID companyId) {
        validateCompanyExists(companyId);
        return notificationRepository.findByCompanyIdAndReadFalseOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<NotificationResponse> getByCompanyAndType(UUID companyId, NotificationType notificationType) {
        validateCompanyExists(companyId);
        return notificationRepository.findByCompanyIdAndNotificationTypeOrderByCreatedAtDesc(companyId, notificationType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long countUnreadByCompanyAndUser(UUID companyId, UUID userId) {
        validateCompanyExists(companyId);
        User user = validateUserInCompany(companyId, userId);
        return notificationRepository.countByCompanyIdAndUserIdAndReadFalse(companyId, user.getId());
    }

    public NotificationResponse markAsRead(UUID id) {
        Notification notification = findById(id);
        if (notification.getUser() != null) {
            validateUserInCompany(notification.getCompany().getId(), notification.getUser().getId());
        }
        notification.markRead();
        return toResponse(notification);
    }

    public List<NotificationResponse> markAllUserNotificationsAsRead(UUID companyId, UUID userId) {
        validateCompanyExists(companyId);
        User user = validateUserInCompany(companyId, userId);

        List<Notification> notifications = notificationRepository.findByCompanyIdAndUserIdOrderByCreatedAtDesc(companyId, user.getId());
        notifications.stream()
                .filter(n -> !n.isRead())
                .forEach(Notification::markRead);

        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    private Notification findById(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
    }

    private void validateCompanyExists(UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    private User validateUserInCompany(UUID companyId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!companyId.equals(user.getCompany().getId())) {
            throw new IllegalArgumentException("User does not belong to the supplied company");
        }

        return user;
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCompany().getId(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
