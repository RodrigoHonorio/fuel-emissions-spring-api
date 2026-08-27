package uk.org.spire.emissionsCalculator.constant;

/**
 * Defines the types of fuel and their specific emission factors.
 * <p>
 * The baseline values are derived from technical inventory methodologies.
 * </p>
 */
public enum FuelType {

    PETROL(0.0015, 15.0, 0.02),
    DIESEL(0.0005, 15.0, 0.01), // Hypothetical values for diesel
    ETHANOL(0.0012, 15.0, 0.015); // Hypothetical values for ethanol

    private final double baseEmissionFactor;
    private final double standardTemperatureCelsius;
    private final double temperatureModifierRate;

    /**
     * Constructor for the enum constants.
     */
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