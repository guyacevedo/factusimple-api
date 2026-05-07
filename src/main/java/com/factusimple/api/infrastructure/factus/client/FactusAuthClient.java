package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.auth.dto.AuthResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class FactusAuthClient {

    private final RestClient restClient = RestClient.create();

    @Value("${factus.url}")
    private String apiUrl;

    @Value("${factus.client_id}")
    private String clientId;

    @Value("${factus.client_secret}")
    private String clientSecret;

    @Value("${factus.user}")
    private String username;

    @Value("${factus.password}")
    private String password;


    public AuthResponseDto generateToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=password&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&username=" + username +
                "&password=" + password;

        try {
            AuthResponseDto response = restClient.post()
                    .uri(apiUrl + "/oauth/token")
                    .headers(h -> h.addAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .body(AuthResponseDto.class);

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
    public AuthResponseDto refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "grant_type=refresh_token&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&refresh_token=" + refreshToken;

        try {
            AuthResponseDto response = restClient.post()
                    .uri(apiUrl + "/oauth/token")
                    .headers(h -> h.addAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .body(AuthResponseDto.class);

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
