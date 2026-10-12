package com.example.emissionsCalculator.dto.spatial;

import java.time.LocalDateTime;

public record HotspotResponseDTO(
        Long id,
        String pollutantType,
        LocalDateTime calculationTimestamp,
        Double intensityScore,
        String severityLevel,
        String sourceInstitution,
        String standardReference,
        Double latitude,
        Double longitude,
        String boroughCode
) {}