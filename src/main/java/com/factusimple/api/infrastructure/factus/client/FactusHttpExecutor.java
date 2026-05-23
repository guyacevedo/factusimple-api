package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactusHttpExecutor {

    static final int MAX_RETRIES = 3;
    static final long INITIAL_BACKOFF_MS = 500L;

    private final RestClient restClient;
    private final FactusTokenService factusTokenService;

    public <T> T executeWithRetry(User user, Supplier<T> operation) {
        Throwable last = null;
        boolean tokenRefreshed = false;
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                return operation.get();
            } catch (HttpClientErrorException.Unauthorized ex) {
                if (tokenRefreshed) {
                    throw new ApiException(502, "Token Factus inválido tras refresh");
                }
                log.warn("Token Factus expirado (401), refrescando...");
                factusTokenService.refreshAndSaveToken(user);
                tokenRefreshed = true;
            } catch (HttpClientErrorException e) {
                String body = e.getResponseBodyAsString();
                log.warn("Factus rechazó la petición ({}): {}", e.getStatusCode(), body);
                throw new ApiException(asGatewayStatus(e.getStatusCode()),
                        "Factus rechazó la petición: " + body);
            } catch (HttpServerErrorException | ResourceAccessException e) {
                last = e;
                log.warn("Reintento {}/{} a Factus falló: {}", attempt + 1, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(INITIAL_BACKOFF_MS * (1L << attempt));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new ApiException(502, "Interrumpido durante retry a Factus");
                    }
                }
            }
        }
        throw new ApiException(502,
                "Factus inalcanzable tras " + MAX_RETRIES + " intentos: "
                        + last.getMessage());
    }

    public <T> T postJson(User user, String path, Object body, Class<T> responseType) {
        String token = factusTokenService.getToken(user);
        return restClient.post()
                .uri(path)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(responseType);
    }

    public <T> T getJson(User user, String path, Class<T> responseType) {
        String token = factusTokenService.getToken(user);
        return restClient.method(HttpMethod.GET)
                .uri(path)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(responseType);
    }

    public <T> T getJson(User user, String path, Class<T> responseType, Object... uriVars) {
        String token = factusTokenService.getToken(user);
        return restClient.method(HttpMethod.GET)
                .uri(path, uriVars)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(responseType);
    }

    public void deleteJson(User user, String path, Object... uriVars) {
        String token = factusTokenService.getToken(user);
        restClient.method(HttpMethod.DELETE)
                .uri(path, uriVars)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity();
    }

    @SuppressWarnings("unchecked")
    public byte[] downloadAsset(User user, String path, String base64Field) {
        Map<String, Object> response = executeWithRetry(user, () -> getJson(user, path, Map.class));
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object base64 = dataMap.get(base64Field);
            if (base64 != null) {
                return Base64.getDecoder().decode(base64.toString());
            }
        }
        throw new ApiException(502, "Respuesta de Factus sin campo '" + base64Field + "'");
    }

    private int asGatewayStatus(HttpStatusCode code) {
        return code.value();
    }
}
