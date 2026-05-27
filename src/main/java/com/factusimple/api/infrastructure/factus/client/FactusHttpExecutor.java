package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.factus.dto.FactusApiResponse;
import com.factusimple.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactusHttpExecutor {

    private static final int MAX_RETRIES = 3;

    private final RestClient restClient;
    private final FactusTokenService tokenService;

    public <T> T executeWithRetry(User user, Supplier<T> operation) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return operation.get();

            } catch (ResourceAccessException ex) {
                log.warn(
                        "Error conectando con Factus. intento={}/{} error={}",
                        attempt,
                        MAX_RETRIES,
                        ex.getMessage()
                );

                sleep(attempt);

            } catch (ApiException ex) {

                if (ex.getStatusCode() == 401 && attempt == 1) {
                    log.warn("Token Factus expirado. refrescando...");

                    tokenService.refreshAndSaveToken(user);

                    continue;
                }

                throw ex;
            }
        }

        throw new ApiException(
                502,
                "Factus no disponible tras " + MAX_RETRIES + " intentos",
                "FACTUS_UNAVAILABLE"
        );
    }

    public <T> T getJson(
            User user,
            String uri,
            Class<T> responseType,
            Object... uriVariables
    ) {

        try {
            return restClient
                    .get()
                    .uri(uri, uriVariables)
                    .headers(headers -> headers.setBearerAuth(tokenService.getToken(user)))
                    .retrieve()
                    .body(responseType);
        } catch (Exception ex) {
            throw handleHttpError(ex);
        }
    }

    public <T> T getJson(
            User user,
            String uri,
            ParameterizedTypeReference<T> responseType,
            Object... uriVariables
    ) {
        try {
            return restClient
                    .get()
                    .uri(uri, uriVariables)
                    .headers(headers -> headers.setBearerAuth(tokenService.getToken(user)))
                    .retrieve()
                    .body(responseType);
        } catch (Exception ex) {
            throw handleHttpError(ex);
        }
    }

    public <T> T postJson(
            User user,
            String uri,
            Object body,
            Class<T> responseType
    ) {

        try {
            return restClient
                    .post()
                    .uri(uri)
                    .headers(headers -> {
                        headers.setBearerAuth(tokenService.getToken(user));
                        headers.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .body(body)
                    .retrieve()
                    .body(responseType);
        } catch (Exception ex) {
            throw handleHttpError(ex);
        }
    }

    public void deleteJson(
            User user,
            String uri,
            Object... uriVariables
    ) {

        try {
            restClient
                    .delete()
                    .uri(uri, uriVariables)
                    .headers(headers -> headers.setBearerAuth(tokenService.getToken(user)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw handleHttpError(ex);
        }
    }

    public byte[] downloadAsset(
            User user,
            String uri,
            String field
    ) {

        FactusApiResponse<Base64AssetResponse> response =
                getJson(user, uri, Base64WrapperResponse.class)
                        .toResponse(field);

        if (response.data() == null || response.data().value() == null) {
            throw new ApiException(
                    502,
                    "Respuesta inválida de Factus"
            );
        }

        return Base64.getDecoder().decode(response.data().value());
    }

    private RuntimeException handleHttpError(Exception ex) {
        if (ex instanceof ApiException apiEx) {
            return apiEx;
        }

        log.error("Error al llamar Factus", ex);
        return new ApiException(
                502,
                "Error al conectar con Factus: " + ex.getMessage()
        );
    }

    private void sleep(int attempt) {
        try {
            Thread.sleep(500L * attempt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public record Base64AssetResponse(String value) {}

    public record Base64WrapperResponse(
            java.util.Map<String, Object> data
    ) {

        public FactusApiResponse<Base64AssetResponse> toResponse(String field) {

            Object value = data.get(field);

            return new FactusApiResponse<>(
                    new Base64AssetResponse(
                            value != null ? value.toString() : null
                    )
            );
        }
    }
}