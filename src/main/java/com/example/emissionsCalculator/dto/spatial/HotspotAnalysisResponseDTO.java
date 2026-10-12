package com.example.emissionsCalculator.dto.spatial;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de resposta consolidada da análise de hotspots para relatórios e auditoria.
 */
public record HotspotAnalysisResponseDTO(
        LocalDateTime analysisTimestamp,
        Integer totalPointsGrid,
        Integer hotspotCount,
        Double maxInterpolatedValue,
        Double hotspotThreshold,
        List<InterpolatedPointDTO> hotspots
) {}