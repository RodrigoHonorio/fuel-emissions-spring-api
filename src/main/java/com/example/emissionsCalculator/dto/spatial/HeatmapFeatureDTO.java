package com.example.emissionsCalculator.dto.spatial;

/**
 * Representa um ponto de intensidade simplificado para renderização de Heatmap no front-end.
 */
public record HeatmapFeatureDTO(
        Double latitude,
        Double longitude,
        Double weight,
        String sourceType,
        String label
) {}
