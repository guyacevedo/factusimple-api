package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.infrastructure.factus.config.FactusProperties;
import com.factusimple.api.infrastructure.factus.dto.FactusAuthResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class FactusAuthClient {

    private final RestClient restClient = RestClient.create();

    private final FactusProperties factusProperties;

    public FactusAuthResponseDto generateToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=password&client_id=" + factusProperties.clientId() +
                "&client_secret=" + factusProperties.clientSecret() +
                "&username=" + factusProperties.user() +
                "&password=" + factusProperties.password();

        try {
            FactusAuthResponseDto response = restClient.post()
                    .uri(factusProperties.url() + "/oauth/token")
                    .headers(h -> h.addAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .body(FactusAuthResponseDto.class);

            if (response != null) {
                log.info("Token generado exitosamente para Factus");
                return response;
            }
        } catch (Exception e) {
            log.error("Error generando token: {}", e.getMessage());
        }

        return null;
    }

    // Generar Refresh Token
    public FactusAuthResponseDto refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=refresh_token&client_id=" + factusProperties.clientId() +
                "&client_secret=" + factusProperties.clientSecret() +
                "&refresh_token=" + refreshToken;

        try {
            FactusAuthResponseDto response = restClient.post()
                    .uri(factusProperties.url() + "/oauth/token")
                    .headers(h -> h.addAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .body(FactusAuthResponseDto.class);

            if (response != null) {
                log.info("Token refrescado exitosamente");
                return response;
            }
        } catch (Exception e) {
            log.error("Error refrescando token: {}", e.getMessage());
        }

        return null;
    }


}
