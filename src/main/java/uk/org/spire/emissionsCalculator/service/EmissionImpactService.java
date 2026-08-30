package uk.org.spire.emissionsCalculator.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.dto.EmissionImpactResponse;
import uk.org.spire.emissionsCalculator.model.SpatialNode;
import uk.org.spire.emissionsCalculator.repository.SpatialNodeRepository;

import java.util.List;

@Service
public class EmissionImpactService {

    private final SpatialNodeRepository spatialNodeRepository;

    public EmissionImpactService(SpatialNodeRepository spatialNodeRepository) {
        this.spatialNodeRepository = spatialNodeRepository;
    }

    public EmissionImpactResponse calculateImpact(String fuelType, double volume, double userLat, double userLon) {
        // 1. Cálculo base de emissão por tipo de combustível (exemplo prático)
        double emissionFactor = getEmissionFactor(fuelType);
        double totalEmissions = volume * emissionFactor;

        // 2. Mapeamento do ponto do usuário usando JTS (SRID 4326)
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point userPoint = geometryFactory.createPoint(new Coordinate(userLon, userLat));

        // 3. Varredura espacial para encontrar a estação de monitoramento mais próxima
        List<SpatialNode> stations = spatialNodeRepository.findAll();
        SpatialNode closestStation = null;
        double minDistance = Double.MAX_VALUE;

        for (SpatialNode station : stations) {
            double distance = station.getLocation().distance(userPoint);
            if (distance < minDistance) {
                minDistance = distance;
                closestStation = station;
            }
        }

        // Conversão aproximada de graus para quilômetros (1 grau na escala de Londres ~ 111 km)
        double distanceKm = minDistance * 111.0;

        // 4. Montagem do DTO de resposta
        EmissionImpactResponse response = new EmissionImpactResponse();
        response.setFuelType(fuelType);
        response.setFuelVolume(volume);
        response.setTotalEmissionsKg(Math.round(totalEmissions * 100.0) / 100.0);
        response.setClosestStationName(closestStation != null ? closestStation.getName() : "Nenhuma estação encontrada");
        response.setDistanceKm(Math.round(distanceKm * 100.0) / 100.0);

        return response;
    }

    private double getEmissionFactor(String fuelType) {
        if ("Petrol".equalsIgnoreCase(fuelType)) return 2.31; // Fator médio kg CO2e/litro
        if ("Diesel".equalsIgnoreCase(fuelType)) return 2.68;
        return 2.0;
    }
}