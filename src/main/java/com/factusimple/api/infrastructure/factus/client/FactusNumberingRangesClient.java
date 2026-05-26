package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.factuscodes.dto.NumberingRangeDto;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.user.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FactusNumberingRangesClient {

    private final FactusHttpExecutor executor;

    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableFallback")
    public NumberingRangeDto getFirstActiveRange(User user) {
        log.debug("Obteniendo rango de numeración activo para usuario: {}", user.getEmail());

        Map<String, Object> response = executor.executeWithRetry(user,
            () -> executor.getJson(user, "/v2/numbering-ranges?filter[document]=21&filter[is_active]=1", Map.class)
        );

        Map<String, Object> item = extractFirstItem(response);
        return mapToDto(item);
    }

    private Map<String, Object> extractFirstItem(Map<String, Object> response) {
        if (response == null) {
            throw new ApiException(502, "Respuesta nula de Factus al obtener rangos de numeración");
        }

        Object data = response.get("data");
        if (!(data instanceof Map<?, ?> dataMap)) {
            throw new ApiException(502, "Estructura inesperada de respuesta de Factus: falta 'data'");
        }

        Object items = dataMap.get("data");
        if (!(items instanceof List<?> itemsList) || itemsList.isEmpty()) {
            throw new ApiException(404, "No hay rangos de numeración activos disponibles en Factus");
        }

        Object firstItem = itemsList.getFirst();
        if (!(firstItem instanceof Map<?, ?>)) {
            throw new ApiException(502, "Primer elemento no es un objeto Map");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> itemMap = (Map<String, Object>) firstItem;
        return itemMap;
    }

    private NumberingRangeDto mapToDto(Map<String, Object> map) {
        NumberingRangeDto dto = new NumberingRangeDto();
        dto.setId(castToInteger(map.get("id")));
        dto.setDocument(castToString(map.get("document")));
        dto.setPrefix(castToString(map.get("prefix")));
        dto.setFrom(castToLong(map.get("from")));
        dto.setTo(castToLong(map.get("to")));
        dto.setCurrent(castToLong(map.get("current")));
        dto.setResolutionNumber(castToString(map.get("resolution_number")));
        dto.setStartDate(castToString(map.get("start_date")));
        dto.setEndDate(castToString(map.get("end_date")));
        dto.setTechnicalKey(castToString(map.get("technical_key")));
        dto.setIsExpired(castToBoolean(map.get("is_expired")));
        dto.setIsActive(castToBoolean(map.get("is_active")));
        return dto;
    }

    private String castToString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Long castToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return null;
    }

    private Integer castToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        return null;
    }

    private Boolean castToBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return Boolean.parseBoolean((String) value);
        return null;
    }

    private NumberingRangeDto factusUnavailableFallback(Exception ex) {
        String message = extractErrorMessage(ex);
        throw new ApiException(503, message, "FACTUS_CIRCUIT_OPEN");
    }

    private String extractErrorMessage(Exception ex) {
        if (ex instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }
        if (ex != null && ex.getMessage() != null) {
            return ex.getMessage();
        }
        return "Factus no disponible temporalmente, reintente en unos minutos";
    }
}
