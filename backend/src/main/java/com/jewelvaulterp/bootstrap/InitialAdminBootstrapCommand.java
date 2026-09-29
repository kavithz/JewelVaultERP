package com.jewelvaulterp.bootstrap;

import com.jewelvaulterp.JewelVaultErpApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

public final class InitialAdminBootstrapCommand {

    private static final String CONFIRMATION_VALUE = "CREATE_INITIAL_ADMIN_ON_EMPTY_NEON";

    private InitialAdminBootstrapCommand() {
    }

    public static void main(String[] args) {
        int exitCode = 0;
        ConfigurableApplicationContext context = null;
        try {
            if (args.length != 0) {
                throw new IllegalArgumentException("This command does not accept command-line arguments.");
            }
            if (!CONFIRMATION_VALUE.equals(requiredEnvironmentVariable("BOOTSTRAP_CONFIRMATION"))) {
                throw new IllegalArgumentException("Explicit bootstrap confirmation is required.");
            }

            String databaseUrl = requiredEnvironmentVariable("DB_URL");
            requireNeonDatabaseUrl(databaseUrl);
            requiredEnvironmentVariable("DB_USERNAME");
            requiredEnvironmentVariable("DB_PASSWORD");
            requiredEnvironmentVariable("JWT_SECRET");

            String username = requiredEnvironmentVariable("BOOTSTRAP_ADMIN_USERNAME");
            String email = optionalEnvironmentVariable("BOOTSTRAP_ADMIN_EMAIL");
            String password = requiredEnvironmentVariable("BOOTSTRAP_ADMIN_PASSWORD");

            System.setProperty("spring.flyway.enabled", "false");
            System.setProperty("spring.main.banner-mode", "off");
            System.setProperty("logging.level.root", "OFF");

            SpringApplicationBuilder application = new SpringApplicationBuilder(JewelVaultErpApplication.class)
                    .web(WebApplicationType.NONE)
                    .initializers(applicationContext -> applicationContext.getEnvironment().getPropertySources()
                            .addFirst(new MapPropertySource("initial-admin-bootstrap-safety", Map.of(
                                    "spring.flyway.enabled", "false",
                                    "spring.main.banner-mode", "off",
                                    "logging.level.root", "OFF"
                            ))));

            context = application.run();
            InitialAdminBootstrapService.BootstrapResult result = context
                    .getBean(InitialAdminBootstrapService.class)
                    .createInitialAdmin(username, email, password);
            System.out.printf("Initial admin bootstrap completed. companyId=%s adminUserId=%s%n",
                    result.companyId(), result.adminUserId());
        } catch (Exception exception) {
            System.err.printf("Initial admin bootstrap failed (%s). No credential values were logged.%n",
                    exception.getClass().getSimpleName());
            exitCode = 1;
        } finally {
            if (context != null) {
                context.close();
            }
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required environment variable is missing: " + name);
        }
        return value;
    }

    private static String optionalEnvironmentVariable(String name) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void requireNeonDatabaseUrl(String jdbcUrl) {
        final String host;
        try {
            if (!jdbcUrl.startsWith("jdbc:postgresql://")) {
                throw new IllegalArgumentException();
            }
            host = URI.create(jdbcUrl.substring("jdbc:".length())).getHost();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("DB_URL must identify a Neon PostgreSQL database.");
        }
        if (host == null || !host.toLowerCase(Locale.ROOT).endsWith(".neon.tech")) {
            throw new IllegalArgumentException("DB_URL must identify a Neon PostgreSQL database.");
        }
    }
}