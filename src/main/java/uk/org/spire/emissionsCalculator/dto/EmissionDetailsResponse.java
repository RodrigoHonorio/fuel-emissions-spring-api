package uk.org.spire.emissionsCalculator.dto;

import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;

/**
 * DTO para transferir os detalhes completos de uma emissão e sua localização espacial.
 */
public record EmissionDetailsResponse(
        Long id,
        double fuelVolumeLitres,
        double ambientTemperatureCelsius,
        double calculatedVocEmissionsKg,
        double latitude,
        double longitude
) {
    /**
     * Factory method para converter a Entidade JPA em DTO de forma segura,
     * extraindo a latitude e longitude do JTS Point do PostGIS.
     */
    public static EmissionDetailsResponse fromEntity(PetrolStationEmission entity) {
        double lat = 0.0;
        double lon = 0.0;

        if (entity.getLocation() != null) {
            lat = entity.getLocation().getY(); // Y = Latitude
            lon = entity.getLocation().getX(); // X = Longitude
        }

        return new EmissionDetailsResponse(
                entity.getId(),
                entity.getFuelVolumeLitres(),
                entity.getAmbientTemperatureCelsius(),
                entity.getCalculatedVocEmissionsKg(),
                lat,
                lon
        );
    }
}