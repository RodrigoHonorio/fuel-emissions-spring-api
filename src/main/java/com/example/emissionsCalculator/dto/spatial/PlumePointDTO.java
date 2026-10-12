package com.example.emissionsCalculator.dto.spatial;

/**
 * Representa um ponto amostrado na malha de dispersão de poluição da pluma.
 */
public record PlumePointDTO(
        Double latitude,
        Double longitude,
        Double concentration,
        Double downwindDistanceMeters,
        Double crosswindDistanceMeters
) {}
