package uk.org.spire.emissionsCalculator.dto;

import uk.org.spire.emissionsCalculator.constant.AqiSeverity;

/**
 * Data Transfer Object representing a spatial node for client-side rendering.
 */
public record SpatialNodeResponse(
        Long id,
        String stationName,
        double latitude,
        double longitude,
        String aqiStatus,
        AqiSeverity severity,
        String markerColour,
        String textColour,
        double ambientTemperatureCelsius,
        double windSpeed,
        double windDirection
) {
}
