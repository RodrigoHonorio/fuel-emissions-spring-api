package com.example.emissionsCalculator.dto.spatial;

/**
 * Representa uma fonte conhecida de emissão/medição (ex: posto de combustível ou estação LAQN).
 */
public record SamplePointDTO(
        Double latitude,
        Double longitude,
        Double value
) {}
