package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.infrastructure.exception.UnauthorizedException;
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

    private final FactusProperties factusProperties;
    private final RestClient restClient;

    public FactusAuthResponseDto generateToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=password&client_id=" + factusProperties.clientId() +
                "&client_secret=" + factusProperties.clientSecret() +
                "&username=" + factusProperties.user() +
                "&password=" + factusProperties.password();

        try {
            FactusAuthResponseDto response = restClient.post()
                    .uri("/oauth/token")
                    .headers(h -> h.addAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .body(FactusAuthResponseDto.class);

            if (response != null) {
                log.info("Token generado exitosamente para Factus");
                return response;
            }
            throw new UnauthorizedException("Respuesta vacía de Factus al generar token");
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generando token de Factus: {}", e.getMessage(), e);
            throw new UnauthorizedException("No fue posible conectar con Factus para generar token: " + e.getMessage());
        }
    }

    public FactusAuthResponseDto refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=refresh_token&client_id=" + factusProperties.clientId() +
                "&client_secret=" + factusProperties.clientSecret() +
                "&refresh_token=" + refreshToken;

        try {
            FactusAuthResponseDto response = restClient.post()
                    .uri("/oauth/token")
                    .headers(h -> h.addAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .body(FactusAuthResponseDto.class);

            if (response != null) {
                log.info("Token refrescado exitosamente");
                return response;
            }
            throw new UnauthorizedException("Respuesta vacía de Factus al refrescar token");
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error refrescando token de Factus: {}", e.getMessage(), e);
            throw new UnauthorizedException("No fue posible conectar con Factus para refrescar token: " + e.getMessage());
        }
    }

}
