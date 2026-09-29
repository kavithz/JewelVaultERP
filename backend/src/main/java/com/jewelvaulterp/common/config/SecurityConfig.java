package com.jewelvaulterp.common.config;

import com.jewelvaulterp.common.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            ObjectMapper objectMapper
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/integration/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(request, response, objectMapper, HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(request, response, objectMapper, HttpStatus.FORBIDDEN)))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") String allowedOrigins,
            @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") String allowedMethods,
            @Value("${app.cors.allowed-headers:Authorization,Content-Type,Accept,Origin,X-Requested-With,X-CSRF-TOKEN}") String allowedHeaders,
            @Value("${app.cors.max-age:3600}") long maxAge,
            @Value("${app.cors.allow-credentials:false}") boolean allowCredentials) {

        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = parseCsv(allowedOrigins);
        if (allowCredentials && origins.contains("*")) {
            throw new IllegalStateException("Credentialed CORS cannot use wildcard origins.");
        }
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(parseCsv(allowedMethods));
        configuration.setAllowedHeaders(parseCsv(allowedHeaders));
        configuration.setExposedHeaders(List.of("Authorization", "Location"));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> parseCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(value -> value.trim())
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private void writeSecurityError(
            HttpServletRequest request,
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                status == HttpStatus.UNAUTHORIZED ? "Authentication is required." : "Access is denied.",
                request.getRequestURI()
        ));
    }
}