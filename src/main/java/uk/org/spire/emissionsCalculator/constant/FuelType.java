package uk.org.spire.emissionsCalculator.constant;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Defines the types of fuel and their specific emission factors.
 */
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_VALUES)
public enum FuelType {

    PETROL(0.0015, 15.0, 0.02),
    DIESEL(0.0005, 15.0, 0.01),
    ETHANOL(0.0012, 15.0, 0.015);

    private final double baseEmissionFactor;
    private final double standardTemperatureCelsius;
    private final double temperatureModifierRate;

    FuelType(double baseEmissionFactor, double standardTemperatureCelsius, double temperatureModifierRate) {
        this.baseEmissionFactor = baseEmissionFactor;
        this.standardTemperatureCelsius = standardTemperatureCelsius;
        this.temperatureModifierRate = temperatureModifierRate;
    }

    public double getBaseEmissionFactor() {
        return baseEmissionFactor;
    }

    public double getStandardTemperatureCelsius() {
        return standardTemperatureCelsius;
    }

    public double getTemperatureModifierRate() {
        return temperatureModifierRate;
    }
}