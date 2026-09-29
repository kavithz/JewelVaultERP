package com.jewelvaulterp.auth.config;

import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class JwtConfig {

    @Bean
    SecretKey jwtSigningKey(@Value("${security.jwt.secret}") String encodedSecret) {
        byte[] secret;
        try {
            secret = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be Base64-encoded.", exception);
        }
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes.");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(jwtSigningKey)
                .keyID("jewelvault-hs256")
                .algorithm(com.nimbusds.jose.JWSAlgorithm.HS256)
                .build();
        ImmutableJWKSet<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    JwtDecoder jwtDecoder(
            SecretKey jwtSigningKey,
            @Value("${security.jwt.issuer:jewelvault-erp}") String issuer
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuer)
        ));
        return decoder;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(UserRepository userRepository) {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            UUID userId;
            UUID companyId;
            try {
                userId = UUID.fromString(jwt.getSubject());
                companyId = UUID.fromString(jwt.getClaimAsString("companyId"));
            } catch (IllegalArgumentException | NullPointerException exception) {
                throw new BadCredentialsException("Invalid bearer token.");
            }

            User user = userRepository.findByIdAndCompanyId(userId, companyId)
                    .filter(User::isActive)
                    .orElseThrow(() -> new BadCredentialsException("Invalid bearer token."));
            return authoritiesConverter.convert(jwt);
        });
        return converter;
    }
}