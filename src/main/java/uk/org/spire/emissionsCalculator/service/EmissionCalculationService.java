package uk.org.spire.emissionsCalculator.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.constant.FuelType;
import uk.org.spire.emissionsCalculator.exception.InvalidEmissionDataException;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;
import uk.org.spire.emissionsCalculator.repository.EmissionRepository;

import java.util.List;

/**
 * Serviço responsável pelo cálculo de emissões veiculares/industriais e
 * simulação de dispersão atmosférica de poluentes para o projeto S.P.I.R.E.
 */
@Service
public class EmissionCalculationService {

    private final EmissionRepository emissionRepository;

    // Fábrica de geometrias JTS configurada com SRID 4326 (WGS 84 / GPS)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    public EmissionCalculationService(EmissionRepository emissionRepository) {
        this.emissionRepository = emissionRepository;
    }

    public PetrolStationEmission calculateAndSaveEmissions(
            FuelType fuelType,
            double volumeLitres,
            double temperatureCelsius,
            double windSpeed,
            double windDirection,
            double latitude,
            double longitude) {

        // Validation rules based on real-world constraints
        if (volumeLitres <= 0) {
            throw new InvalidEmissionDataException("Fuel volume must be greater than zero.");
        }
        if (temperatureCelsius < -50 || temperatureCelsius > 60) {
            throw new InvalidEmissionDataException("Ambient temperature is out of realistic physical bounds for London.");
        }
        if (windSpeed < 0) {
            throw new InvalidEmissionDataException("Wind speed cannot be negative.");
        }
        if (windDirection < 0 || windDirection > 360) {
            throw new InvalidEmissionDataException("Wind direction must be between 0 and 360 degrees.");
        }

        // Temperature adjustment calculations
        double tempDifference = temperatureCelsius - fuelType.getStandardTemperatureCelsius();
        double temperatureModifier = 1.0 + (tempDifference * fuelType.getTemperatureModifierRate());

        double totalVocEmissionsKg = volumeLitres * fuelType.getBaseEmissionFactor() * temperatureModifier;

        // Atmospheric Dispersion Calculation (Gaussian Plume Model principles)
        double effectiveWindSpeed = Math.max(windSpeed, 0.5);
        double dispersionFactor = 1.0 / effectiveWindSpeed;

        double dispersedEmissionsKg = totalVocEmissionsKg * dispersionFactor;

        // Criando o Ponto Geográfico PostGIS (Ordem: Longitude, Latitude -> X, Y)
        Point stationLocation = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        PetrolStationEmission emissionRecord = new PetrolStationEmission(
                volumeLitres,
                temperatureCelsius,
                dispersedEmissionsKg,
                stationLocation
        );

        return emissionRepository.save(emissionRecord);
    }

    /**
     * Busca estações de emissão dentro de um raio geográfico específico utilizando PostGIS.
     *
     * @param latitude         Latitude central de referência em Londres.
     * @param longitude        Longitude central de referência em Londres.
     * @param distanceInMeters Raio de busca em metros.
     * @return Lista de registros de emissão encontrados na área.
     */
    public List<PetrolStationEmission> getEmissionsWithinRadius(double latitude, double longitude, double distanceInMeters) {
        if (distanceInMeters <= 0) {
            throw new InvalidEmissionDataException("Search distance radius must be greater than zero.");
        }
        return emissionRepository.findEmissionsWithinRadius(latitude, longitude, distanceInMeters);
    }
}