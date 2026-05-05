package com.factusimple.api.municipality.service;

import com.factusimple.api.municipality.dto.MunicipalityDto;
import com.factusimple.api.municipality.dto.MunicipalityResponseDto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


import java.io.InputStream;
import java.util.List;

@Service
public class MunicipalityService {

    private final ObjectMapper objectMapper;

    private List<MunicipalityDto> cache;

    public MunicipalityService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadData() {
        InputStream is = getClass()
                .getResourceAsStream("/data/municipalities.json");

        MunicipalityResponseDto response =
                objectMapper.readValue(is, MunicipalityResponseDto.class);

        this.cache = response.getMunicipalities();
    }

    public List<MunicipalityDto> getAll() {
        return cache;
    }
}
