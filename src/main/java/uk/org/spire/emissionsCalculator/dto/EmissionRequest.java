package uk.org.spire.emissionsCalculator.dto;

import uk.org.spire.emissionsCalculator.constant.FuelType;

public record EmissionRequest(
        FuelType fuelType,
        double fuelVolumeLitres,
        double ambientTemperatureCelsius,
        double windSpeed,
        double windDirection,
        double latitude,
        double longitude
) {}