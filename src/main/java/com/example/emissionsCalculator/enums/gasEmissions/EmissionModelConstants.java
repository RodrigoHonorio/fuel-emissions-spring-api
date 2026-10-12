package com.example.emissionsCalculator.enums.gasEmissions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <b>O que é:</b> Enumeração de constantes físicas e químicas para o modelo de emissões evaporativas (EPA AP-42).
 * <b>Para que serve:</b> Centralizar parâmetros científicos, fatores de conversão e valores padrão.
 */
@Getter
@RequiredArgsConstructor
public enum EmissionModelConstants {

    // Clima Padrão
    DEFAULT_TEMPERATURE_CELSIUS(15.0),
    DEFAULT_WIND_SPEED_KMH(10.0),

    // Parâmetros de Posto
    DEFAULT_THROUGHPUT_LITERS_PER_DAY(5000.0),

    // Fatores de Emissão e Físico-Químicos
    SPILLAGE_FACTOR_G_PER_L(0.08),           // Fator fixo de derramamento (~0.08g/L)
    BENZENE_FRACTION_IN_VAPOR(0.01),        // ~1% de Benzeno na fração de vapor
    CELSIUS_TO_KELVIN_OFFSET(273.15),       // Conversão de Celsius para Kelvin
    GRAMS_TO_KILOGRAMS_DIVISOR(1000.0),     // Divisor para conversão g -> kg

    // Parâmetros das Equações EPA AP-42 e Antoine
    ANTOINE_BASE_MULTIPLIER(10.0),
    ANTOINE_EXPONENT_FACTOR(0.04),
    EMISSION_FACTOR_MULTIPLIER(0.120),
    EMISSION_FACTOR_MOLECULAR_WEIGHT_FACTOR(66.0);

    private final double value;
}