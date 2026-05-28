package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.factuscodes.dto.NumberingRangeDto;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.factus.dto.FactusApiResponse;
import com.factusimple.api.infrastructure.factus.dto.FactusPaginatedResponse;
import com.factusimple.api.user.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FactusNumberingRangesClient {

    private final FactusHttpExecutor executor;

    @Retry(name = "factus")
    @CircuitBreaker(
            name = "factus",
            fallbackMethod = "factusUnavailableFallback"
    )
    public NumberingRangeDto getFirstActiveRange(
            User user
    ) {

        log.debug(
                "Obteniendo rango de numeración activo para usuario: {}",
                user.getEmail()
        );

        var response = executor.getJson(
                user,
                "/v2/numbering-ranges?filter[document]=21&filter[is_active]=1",
                new ParameterizedTypeReference<
                        FactusApiResponse<
                                FactusPaginatedResponse<NumberingRangeDto>
                                >
                        >() {}
        );

        return response.data()
                .data()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new ApiException(
                                404,
                                "No hay rangos de numeración activos disponibles"
                        )
                );
    }

    private NumberingRangeDto factusUnavailableFallback(
            User user,
            Exception ex
    ) {

        throw new ApiException(
                503,
                extractErrorMessage(ex),
                "FACTUS_CIRCUIT_OPEN"
        );
    }

    private String extractErrorMessage(Exception ex) {

        if (ex instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }

        return ex != null && ex.getMessage() != null
                ? ex.getMessage()
                : "Factus no disponible temporalmente";
    }
}