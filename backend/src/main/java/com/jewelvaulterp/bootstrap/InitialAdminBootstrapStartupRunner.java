package com.jewelvaulterp.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminBootstrapStartupRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialAdminBootstrapStartupRunner.class);

    private final InitialAdminBootstrapService bootstrapService;
    private final String adminUsername;
    private final String adminPassword;
    private final String renderMarker;
    private final String renderGitBranch;
    private final String renderPullRequestMarker;

    public InitialAdminBootstrapStartupRunner(
            InitialAdminBootstrapService bootstrapService,
            @Value("${DEV_ADMIN_USERNAME:}") String adminUsername,
            @Value("${DEV_ADMIN_PASSWORD:}") String adminPassword,
            @Value("${RENDER:false}") String renderMarker,
            @Value("${RENDER_GIT_BRANCH:}") String renderGitBranch,
            @Value("${IS_PULL_REQUEST:false}") String renderPullRequestMarker
    ) {
        this.bootstrapService = bootstrapService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.renderMarker = renderMarker;
        this.renderGitBranch = renderGitBranch;
        this.renderPullRequestMarker = renderPullRequestMarker;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!"true".equalsIgnoreCase(renderMarker)
                || !"main".equals(renderGitBranch)
                || "true".equalsIgnoreCase(renderPullRequestMarker)) {
            return;
        }

        boolean hasUsername = adminUsername != null && !adminUsername.isBlank();
        boolean hasPassword = adminPassword != null && !adminPassword.isBlank();
        if (!hasUsername && !hasPassword) {
            return;
        }
        if (!hasUsername || !hasPassword) {
            throw new IllegalStateException(
                    "DEV_ADMIN_USERNAME and DEV_ADMIN_PASSWORD must both be configured for production bootstrap."
            );
        }

        try {
            InitialAdminBootstrapService.BootstrapResult result = bootstrapService.createInitialAdmin(
                    adminUsername.trim(), null, adminPassword
            );
            LOGGER.info("Initial admin bootstrap completed for companyId={} adminUserId={}",
                    result.companyId(), result.adminUserId());
        } catch (InitialAdminBootstrapService.BootstrapAlreadyInitializedException exception) {
            LOGGER.info("Initial admin bootstrap skipped because tenant or identity data already exists.");
        }
    }
}