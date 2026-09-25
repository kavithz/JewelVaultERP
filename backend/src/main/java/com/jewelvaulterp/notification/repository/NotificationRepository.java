package com.jewelvaulterp.notification.repository;

import com.jewelvaulterp.notification.entity.Notification;
import com.jewelvaulterp.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    List<Notification> findByCompanyIdAndUserIdOrderByCreatedAtDesc(UUID companyId, UUID userId);

    List<Notification> findByCompanyIdAndUserIdAndReadFalseOrderByCreatedAtDesc(UUID companyId, UUID userId);

    List<Notification> findByCompanyIdAndReadFalseOrderByCreatedAtDesc(UUID companyId);

    List<Notification> findByCompanyIdAndNotificationTypeOrderByCreatedAtDesc(UUID companyId, NotificationType notificationType);

    long countByCompanyIdAndUserIdAndReadFalse(UUID companyId, UUID userId);
}
