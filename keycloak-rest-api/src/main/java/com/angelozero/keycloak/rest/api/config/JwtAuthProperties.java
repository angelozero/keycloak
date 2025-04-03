package com.angelozero.keycloak.rest.api.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties
public class JwtAuthProperties {

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    @Value("${jwt.auth.converter.principle-attribute}")
    private String principleAttribute;

    @Value("${jwt.auth.token.url}")
    private String tokenUrl;

    @Value("${jwt.auth.token.client-id}")
    private String clientId;

    @Value("${jwt.auth.token.client-secret}")
    private String clientSecret;
}
