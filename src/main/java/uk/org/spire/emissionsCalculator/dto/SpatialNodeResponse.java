package uk.org.spire.emissionsCalculator.dto;

/**
 * Data Transfer Object representing a spatial node for client-side rendering.
 *
 * @param id          The unique identifier of the node.
 * @param stationName The name of the monitoring station.
 * @param latitude    The latitude extracted from the JTS point.
 * @param longitude   The longitude extracted from the JTS point.
 */
public record SpatialNodeResponse(
        Long id,
        String stationName,
        double latitude,
        double longitude
) {
}