package com.example.emissionsCalculator.service.gasEmissions.calculator;

import com.example.emissionsCalculator.service.gasEmissions.constants.EmissionModelConstants;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Calculador físico-químico isolado para emissões evaporativas.
 * <b>Para que serve:</b> Executar as equações matemáticas da EPA AP-42 sem acoplamento com a camada de serviço do Spring.
 */
public class EvaporativeEmissionsCalculator {

    private EvaporativeEmissionsCalculator() {
        // Construtor privado para classe utilitária
    }

    /**
     * Calcula a Pressão Verdadeira de Vapor (kPa) via equação adaptada de Antoine.
     */
    public static double calculateTrueVaporPressure(double temperatureCelsius) {
        return EmissionModelConstants.ANTOINE_BASE_MULTIPLIER.getValue()
                * Math.exp(EmissionModelConstants.ANTOINE_EXPONENT_FACTOR.getValue() * temperatureCelsius);
    }

    /**
     * Calcula o Fator de Emissão por Evaporação (g/L).
     */
    public static double calculateEmissionFactor(double temperatureCelsius, double trueVaporPressure) {
        double tempKelvin = temperatureCelsius + EmissionModelConstants.CELSIUS_TO_KELVIN_OFFSET.getValue();
        return (EmissionModelConstants.EMISSION_FACTOR_MULTIPLIER.getValue()
                * trueVaporPressure
                * EmissionModelConstants.EMISSION_FACTOR_MOLECULAR_WEIGHT_FACTOR.getValue()) / tempKelvin;
    }

    /**
     * Instancia e calcula o objeto GasStationEmission para um determinado posto de combustível.
     */
    public static GasStationEmission calculateForStation(long stationId,
                                                         double tempC,
                                                         double windKmh,
                                                         double throughputLiters,
                                                         double emissionFactor,
                                                         LocalDateTime timestamp) {

        double evaporativeVocKg = (emissionFactor * throughputLiters)
                / EmissionModelConstants.GRAMS_TO_KILOGRAMS_DIVISOR.getValue();

        double spillageVocKg = (EmissionModelConstants.SPILLAGE_FACTOR_G_PER_L.getValue() * throughputLiters)
                / EmissionModelConstants.GRAMS_TO_KILOGRAMS_DIVISOR.getValue();

        double totalVocKg = evaporativeVocKg + spillageVocKg;

        double benzeneGrams = totalVocKg
                * EmissionModelConstants.GRAMS_TO_KILOGRAMS_DIVISOR.getValue()
                * EmissionModelConstants.BENZENE_FRACTION_IN_VAPOR.getValue();

        return GasStationEmission.builder()
                .gasStationId(stationId)
                .calculationTimestamp(timestamp)
                .temperatureCelsius(tempC)
                .windSpeedKmh(windKmh)
                .estimatedDailyThroughputLiters(throughputLiters)
                .evaporativeVocKg(evaporativeVocKg)
                .spillageVocKg(spillageVocKg)
                .totalVocKg(totalVocKg)
                .benzeneEmissionsG(benzeneGrams)
                .build();
    }
}
