package com.jewelvaulterp.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InitialAdminBootstrapStartupRunnerTest {

    private final InitialAdminBootstrapService bootstrapService = mock(InitialAdminBootstrapService.class);
    private final DefaultApplicationArguments arguments = new DefaultApplicationArguments(new String[0]);

    @Test
    void skipsOutsideRenderProductionMain() {
        runner("admin", "test-only-password", "false", "main", "false").run(arguments);
        runner("admin", "test-only-password", "true", "feature/test", "false").run(arguments);
        runner("admin", "test-only-password", "true", "main", "true").run(arguments);

        verifyNoInteractions(bootstrapService);
    }

    @Test
    void skipsWhenBootstrapCredentialsAreNotConfigured() {
        runner("", "", "true", "main", "false").run(arguments);

        verifyNoInteractions(bootstrapService);
    }

    @Test
    void callsBootstrapWithUsernameAndPasswordButNoEmail() {
        when(bootstrapService.createInitialAdmin("admin", null, "test-only-password"))
            .thenReturn(new InitialAdminBootstrapService.BootstrapResult(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
            ));

        runner(" admin ", "test-only-password", "true", "main", "false").run(arguments);

        verify(bootstrapService).createInitialAdmin("admin", null, "test-only-password");
    }

    @Test
    void partialCredentialsFailWithoutCallingBootstrap() {
        assertThrows(IllegalStateException.class,
                () -> runner("admin", "", "true", "main", "false").run(arguments));

        verify(bootstrapService, never()).createInitialAdmin(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void existingDatabaseIsSkippedWithoutFailingStartup() {
        when(bootstrapService.createInitialAdmin("admin", null, "test-only-password"))
                .thenThrow(new InitialAdminBootstrapService.BootstrapAlreadyInitializedException());

        runner("admin", "test-only-password", "true", "main", "false").run(arguments);

        verify(bootstrapService).createInitialAdmin("admin", null, "test-only-password");
    }

    private InitialAdminBootstrapStartupRunner runner(
            String username,
            String password,
            String render,
            String branch,
            String pullRequest
    ) {
        return new InitialAdminBootstrapStartupRunner(
                bootstrapService, username, password, render, branch, pullRequest
        );
    }
}