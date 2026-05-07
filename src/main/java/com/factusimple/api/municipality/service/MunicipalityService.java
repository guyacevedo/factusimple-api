package com.factusimple.api.municipality.service;

import com.factusimple.api.municipality.dto.MunicipalityDto;
import com.factusimple.api.municipality.dto.MunicipalityResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MunicipalityService {

    private final ObjectMapper objectMapper;

    private List<MunicipalityDto> municipalitiesCache;

    public MunicipalityService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadData()  {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/municipalities.json")) {
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo municipalities.json");
            }

            MunicipalityResponseDto response = objectMapper.readValue(inputStream, MunicipalityResponseDto.class);
            this.municipalitiesCache = Collections.unmodifiableList(response.getMunicipalities());
            log.info("Municipalities cargados en caché exitosamente");
        } catch (IOException e) {
            log.error("Error al cargar datos de municipios", e);
        }
    }

    public List<MunicipalityDto> getAll() {
        return municipalitiesCache;
    }
}
