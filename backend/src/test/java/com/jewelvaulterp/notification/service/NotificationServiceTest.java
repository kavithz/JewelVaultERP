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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Company companyA;
    private Company companyB;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        companyA = new Company(UUID.randomUUID(), "Acme", "Acme Ltd", "US", "USD", LocalDateTime.now(), LocalDateTime.now());
        companyB = new Company(UUID.randomUUID(), "Beta", "Beta Ltd", "UK", "GBP", LocalDateTime.now(), LocalDateTime.now());

        userA = new User(UUID.randomUUID(), companyA, "alice", "alice@example.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
        userB = new User(UUID.randomUUID(), companyB, "bob", "bob@example.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void create_shouldPersistNotificationForCompanyUser() {
        UUID referenceId = UUID.randomUUID();
        when(companyRepository.findById(companyA.getId())).thenReturn(Optional.of(companyA));
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.create(
                new CreateNotificationRequest(
                        companyA.getId(),
                        userA.getId(),
                        NotificationType.SYSTEM,
                        "Welcome",
                        "Your account is ready.",
                        "user",
                        referenceId
                )
        );

        assertNotNull(response);
        assertEquals(companyA.getId(), response.companyId());
        assertEquals(userA.getId(), response.userId());
        assertEquals("Welcome", response.title());
        assertFalse(response.read());
    }

    @Test
    void create_shouldRejectUserFromDifferentCompany() {
        when(companyRepository.findById(companyA.getId())).thenReturn(Optional.of(companyA));
        when(userRepository.findById(userB.getId())).thenReturn(Optional.of(userB));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.create(
                        new CreateNotificationRequest(
                                companyA.getId(),
                                userB.getId(),
                                NotificationType.SYSTEM,
                                "Blocked",
                                "This should fail.",
                                null,
                                null
                        )
                )
        );

        assertTrue(ex.getMessage().contains("company"));
    }

    @Test
    void getUnreadByCompanyAndUser_shouldReturnUnreadOnly() {
        Notification unread = new Notification(
                UUID.randomUUID(), companyA, userA, NotificationType.PAYMENT_DUE,
                "Payment due", "Invoice payment is due soon.", "invoice",
                UUID.randomUUID(), false, LocalDateTime.now(), null
        );
        Notification read = new Notification(
                UUID.randomUUID(), companyA, userA, NotificationType.SYSTEM,
                "Welcome", "Welcome back.", "account",
                UUID.randomUUID(), true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(companyRepository.findById(companyA.getId())).thenReturn(Optional.of(companyA));
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(notificationRepository.findByCompanyIdAndUserIdAndReadFalseOrderByCreatedAtDesc(companyA.getId(), userA.getId()))
                .thenReturn(List.of(unread));

        List<NotificationResponse> responses = notificationService.getUnreadByCompanyAndUser(companyA.getId(), userA.getId());

        assertEquals(1, responses.size());
        assertEquals(unread.getId(), responses.get(0).id());
        assertFalse(responses.get(0).read());
    }

    @Test
    void markAllUserNotificationsAsRead_shouldMarkAllUnreadForUser() {
        Notification unreadOne = new Notification(
                UUID.randomUUID(), companyA, userA, NotificationType.PAYMENT_DUE,
                "Payment due", "Invoice is due.", "invoice",
                UUID.randomUUID(), false, LocalDateTime.now(), null
        );
        Notification unreadTwo = new Notification(
                UUID.randomUUID(), companyA, userA, NotificationType.RECEIVABLE_DUE,
                "Receivable due", "Customer payment is due.", "customer",
                UUID.randomUUID(), false, LocalDateTime.now(), null
        );

        when(companyRepository.findById(companyA.getId())).thenReturn(Optional.of(companyA));
        when(userRepository.findById(userA.getId())).thenReturn(Optional.of(userA));
        when(notificationRepository.findByCompanyIdAndUserIdOrderByCreatedAtDesc(companyA.getId(), userA.getId()))
                .thenReturn(List.of(unreadOne, unreadTwo));

        List<NotificationResponse> responses = notificationService.markAllUserNotificationsAsRead(companyA.getId(), userA.getId());

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(NotificationResponse::read));
    }
}
