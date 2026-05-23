package com.factusimple.api.infrastructure.factus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class FactusRestClientConfig {

    private final FactusProperties factusProperties;

    @Bean
    public RestClient factusRestClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(30));

        return builder
                .baseUrl(factusProperties.url())
                .requestFactory(factory)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}