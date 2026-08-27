package uk.org.spire.emissionsCalculator.dto;

import uk.org.spire.emissionsCalculator.constant.FuelType;

/**
 * Data Transfer Object (DTO) for receiving emission calculation requests.
 */
public record EmissionRequest(
        FuelType fuelType,
        double fuelVolumeLitres,
        double ambientTemperatureCelsius
) {
}