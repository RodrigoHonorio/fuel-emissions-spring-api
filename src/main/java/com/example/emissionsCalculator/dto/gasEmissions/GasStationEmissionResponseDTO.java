package com.example.emissionsCalculator.dto.gasEmissions;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Record DTO de resposta para exibição dos cálculos de emissão.
 * <b>Para que serve:</b> Transferir os dados de emissão calculados para o frontend ou Swagger.
 */
public record GasStationEmissionResponseDTO(
        Long id,
        Long gasStationId,
        LocalDateTime calculationTimestamp,
        Double temperatureCelsius,
        Double windSpeedKmh,
        Double estimatedDailyThroughputLiters,
        Double evaporativeVocKg,
        Double spillageVocKg,
        Double totalVocKg,
        Double benzeneEmissionsG
) {}
