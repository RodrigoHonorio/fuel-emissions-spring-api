package com.example.emissionsCalculator.dto.scientific;

import java.time.LocalDateTime;

public record SimulationBatchResponseDTO(
        Long id,
        String batchName,
        String articleName,
        LocalDateTime executionTimestamp,
        String horizonType,
        Integer lookbackDays,
        LocalDateTime historyStartTime,
        LocalDateTime startForecastTime,
        LocalDateTime endForecastTime,
        Long parentSimulationId,
        String status
) {}