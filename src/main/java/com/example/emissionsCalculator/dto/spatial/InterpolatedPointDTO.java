package com.example.emissionsCalculator.dto.spatial;

/**
 * Representa um ponto amostrado na malha de interpolação espacial IDW.
 */
public record InterpolatedPointDTO(
        Double latitude,
        Double longitude,
        Double interpolatedValue,
        Boolean isHotspot
) {}
