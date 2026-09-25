package com.jewelvaulterp.audit.service;

import com.jewelvaulterp.audit.dto.AuditLogResponse;
import com.jewelvaulterp.audit.dto.CreateAuditLogRequest;
import com.jewelvaulterp.audit.entity.AuditAction;
import com.jewelvaulterp.audit.entity.AuditLog;
import com.jewelvaulterp.audit.repository.AuditLogRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    private AuditLogService service;
    private Company companyOne;
    private Company companyTwo;
    private User userOne;
    private User userTwo;

    @BeforeEach
    void setUp() {
        service = new AuditLogService(auditLogRepository, companyRepository, userRepository);

        companyOne = new Company(
                UUID.randomUUID(),
                "Acme",
                "Acme Holdings",
                "US",
                "USD",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        companyTwo = new Company(
                UUID.randomUUID(),
                "Beta",
                "Beta Ltd",
                "CA",
                "CAD",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userOne = new User(
                UUID.randomUUID(),
                companyOne,
                "alice",
                "alice@example.com",
                "hash",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userTwo = new User(
                UUID.randomUUID(),
                companyTwo,
                "bob",
                "bob@example.com",
                "hash",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void createsAuditLogSuccessfully() {
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                companyOne.getId(),
                userOne.getId(),
                AuditAction.LOGIN,
                "User",
                userOne.getId(),
                "User login",
                null,
                null,
                "127.0.0.1",
                "Mozilla/5.0"
        );

        when(companyRepository.findById(companyOne.getId())).thenReturn(Optional.of(companyOne));
        when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLogResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(companyOne.getId(), response.companyId());
        assertEquals(userOne.getId(), response.userId());
        assertEquals(AuditAction.LOGIN, response.action());
        assertEquals("User", response.entityType());
        assertEquals(userOne.getId(), response.entityId());
        assertNotNull(response.createdAt());
    }

    @Test
    void rejectsUnknownCompany() {
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                UUID.randomUUID(),
                userOne.getId(),
                AuditAction.CREATE,
                "Order",
                UUID.randomUUID(),
                "desc",
                null,
                null,
                "127.0.0.1",
                "UA"
        );

        when(companyRepository.findById(request.companyId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    @Test
    void rejectsUnknownUser() {
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                companyOne.getId(),
                UUID.randomUUID(),
                AuditAction.CREATE,
                "Order",
                UUID.randomUUID(),
                "desc",
                null,
                null,
                "127.0.0.1",
                "UA"
        );

        when(companyRepository.findById(companyOne.getId())).thenReturn(Optional.of(companyOne));
        when(userRepository.findById(request.userId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    @Test
    void rejectsUserBelongingToAnotherCompany() {
        CreateAuditLogRequest request = new CreateAuditLogRequest(
                companyOne.getId(),
                userTwo.getId(),
                AuditAction.CREATE,
                "Order",
                UUID.randomUUID(),
                "desc",
                null,
                null,
                "127.0.0.1",
                "UA"
        );

        when(companyRepository.findById(companyOne.getId())).thenReturn(Optional.of(companyOne));
        when(userRepository.findById(userTwo.getId())).thenReturn(Optional.of(userTwo));

        assertThrows(IllegalArgumentException.class, () -> service.create(request));
    }

    @Test
    void retrievesLogsByCompany() {
        when(companyRepository.findById(companyOne.getId())).thenReturn(Optional.of(companyOne));
        when(auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyOne.getId())).thenReturn(List.of(
                log(companyOne, userOne, AuditAction.LOGIN, "User", userOne.getId()),
                log(companyOne, userOne, AuditAction.UPDATE, "Order", UUID.randomUUID())
        ));

        List<AuditLogResponse> result = service.getByCompany(companyOne.getId());

        assertEquals(2, result.size());
        assertEquals(companyOne.getId(), result.get(0).companyId());
    }

    @Test
    void retrievesLogsByUserWithCompanyIsolation() {
        when(companyRepository.findById(companyOne.getId())).thenReturn(Optional.of(companyOne));
        when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
        when(auditLogRepository.findByCompanyIdAndUserIdOrderByCreatedAtDesc(companyOne.getId(), userOne.getId()))
                .thenReturn(List.of(log(companyOne, userOne, AuditAction.LOGIN, "User", userOne.getId())));

        List<AuditLogResponse> result = service.getByCompanyAndUser(companyOne.getId(), userOne.getId());

        assertEquals(1, result.size());
        assertEquals(userOne.getId(), result.get(0).userId());
    }

    @Test
    void retrievesLogsByEntityWithCompanyIsolation() {
        UUID entityId = UUID.randomUUID();
        when(companyRepository.findById(companyOne.getId())).thenReturn(Optional.of(companyOne));
        when(auditLogRepository.findByCompanyIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                companyOne.getId(),
                "Order",
                entityId
        )).thenReturn(List.of(log(companyOne, userOne, AuditAction.UPDATE, "Order", entityId)));

        List<AuditLogResponse> result = service.getByCompanyAndEntity(companyOne.getId(), "Order", entityId);

        assertEquals(1, result.size());
        assertEquals(entityId, result.get(0).entityId());
    }

    private AuditLog log(Company company, User user, AuditAction action, String entityType, UUID entityId) {
        return new AuditLog(
                UUID.randomUUID(),
                company,
                user,
                action,
                entityType,
                entityId,
                "audit description",
                null,
                null,
                "127.0.0.1",
                "UA",
                LocalDateTime.now()
        );
    }
}
