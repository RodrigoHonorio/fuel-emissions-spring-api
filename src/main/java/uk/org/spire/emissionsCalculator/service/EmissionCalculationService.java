package uk.org.spire.emissionsCalculator.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.constant.FuelType;
import uk.org.spire.emissionsCalculator.model.GasStation;
import uk.org.spire.emissionsCalculator.model.PetrolStationEmission;
import uk.org.spire.emissionsCalculator.repository.EmissionRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelo cálculo e persistência das emissões de Compostos Orgânicos Voláteis (COV / VOC).
 *
 * Referências Técnicas e Científicas:
 * 1. US EPA AP-42, Chapter 5.2: "Transportation and Marketing of Petroleum Liquids"
 *    Link: https://www.epa.gov/air-emissions-factors-and-quantification/ap-42-compilation-air-emissions-factors
 * 2. CETESB - Metodologia de Inventário de Evaporação de Combustível no Abastecimento
 *    Link: https://cetesb.sp.gov.br/
 */
@Service
public class EmissionCalculationService {

    private static final double BASE_VOC_FACTOR_G_PER_L = 0.139;
    private static final double AVERAGE_THROUGHPUT_PER_PUMP_LITERS = 3000.0;

    private final EmissionRepository emissionRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public EmissionCalculationService(EmissionRepository emissionRepository) {
        this.emissionRepository = emissionRepository;
    }

    /**
     * Calcula e persiste uma nova medição no banco criando um ponto espacial JTS.
     */
    public PetrolStationEmission calculateAndSaveEmissions(
            FuelType fuelType,
            double fuelVolumeLitres,
            double ambientTemperatureCelsius,
            double windSpeed,
            double windDirection,
            double latitude,
            double longitude
    ) {
        double tempFactor = Math.max(0.5, 1.0 + 0.02 * (ambientTemperatureCelsius - 20.0));
        double fuelFactor = (fuelType == FuelType.DIESEL) ? 0.05 : 1.0;

        double emissionGrams = fuelVolumeLitres * BASE_VOC_FACTOR_G_PER_L * tempFactor * fuelFactor;
        double emissionKgPerDay = Math.round((emissionGrams / 1000.0) * 100.0) / 100.0;

        // Cria o ponto geométrico PostGIS (Longitude, Latitude)
        Point locationPoint = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        PetrolStationEmission record = new PetrolStationEmission(
                fuelVolumeLitres,
                ambientTemperatureCelsius,
                emissionKgPerDay,
                locationPoint
        );

        return emissionRepository.save(record);
    }

    /**
     * Retorna todas as emissões cadastradas num determinado raio (km).
     */
    public List<PetrolStationEmission> getEmissionsWithinRadius(double latitude, double longitude, double radiusKm) {
        return emissionRepository.findAll().stream()
                .filter(e -> {
                    if (e.getLocation() != null) {
                        double eLat = e.getLocation().getY();
                        double eLon = e.getLocation().getX();
                        return haversineDistance(latitude, longitude, eLat, eLon) <= radiusKm;
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcula a emissão diária de COV (em kg/dia) para uma entidade GasStation.
     */
    public double calculateVocForStation(GasStation station, double temp, double humidity, double pressure, double solarRad, double windSpeed, String fuelType) {
        if (station == null) {
            return 0.0;
        }

        double dailyVolumeLiters;
        if (station.getDailyThroughput() != null && station.getDailyThroughput() > 0) {
            dailyVolumeLiters = station.getDailyThroughput() * 1000.0;
        } else {
            int pumps = (station.getPumpCount() != null && station.getPumpCount() > 0) ? station.getPumpCount() : 4;
            dailyVolumeLiters = pumps * AVERAGE_THROUGHPUT_PER_PUMP_LITERS;
        }

        double tempFactor = Math.max(0.5, 1.0 + 0.02 * (temp - 20.0));
        double fuelFactor;
        if ("diesel".equalsIgnoreCase(fuelType)) {
            fuelFactor = 0.05;
        } else if ("gasoline".equalsIgnoreCase(fuelType)) {
            fuelFactor = 1.0;
        } else {
            fuelFactor = 0.85;
        }

        double emissionGrams = dailyVolumeLiters * BASE_VOC_FACTOR_G_PER_L * tempFactor * fuelFactor;
        double emissionKgPerDay = emissionGrams / 1000.0;

        return Math.round(emissionKgPerDay * 100.0) / 100.0;
    }

    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
}