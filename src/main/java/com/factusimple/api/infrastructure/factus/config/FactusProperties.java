package com.factusimple.api.infrastructure.factus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "factus")
public record FactusProperties(
        String url,
        String clientId,
        String clientSecret,
        String user,
        String password,
        String expectedAudience
) {}