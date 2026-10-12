package com.example.emissionsCalculator.engine.spatial;

import com.example.emissionsCalculator.dto.spatial.InterpolatedPointDTO;
import com.example.emissionsCalculator.dto.spatial.SamplePointDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpatialInterpolationEngine {

    private static final double METERS_PER_DEGREE_LAT = 111320.0;
    private static final double DEFAULT_POWER_P = 2.0; // Expoente padrão do IDW

    /**
     * Realiza a interpolação IDW em uma malha retangular em torno de Londres.
     *
     * @param samplePoints Lista de pontos conhecidos com valores medidos/calculados
     * @param minLat Latitude mínima da caixa delimitadora (Bounding Box)
     * @param maxLat Latitude máxima da caixa delimitadora
     * @param minLon Longitude mínima da caixa delimitadora
     * @param maxLon Longitude máxima da caixa delimitadora
     * @param gridSize Passos da malha em graus (ex: 0.005 para ~500 metros)
     * @param hotspotThreshold Limite de corte para definir se o ponto é um hotspot crítico
     * @return Lista de pontos interpolados na malha regular
     */
    public List<InterpolatedPointDTO> interpolateGrid(
            List<SamplePointDTO> samplePoints,
            double minLat,
            double maxLat,
            double minLon,
            double maxLon,
            double gridSize,
            double hotspotThreshold
    ) {
        List<InterpolatedPointDTO> grid = new ArrayList<>();

        if (samplePoints == null || samplePoints.isEmpty()) {
            return grid;
        }

        for (double lat = minLat; lat <= maxLat; lat += gridSize) {
            for (double lon = minLon; lon <= maxLon; lon += gridSize) {

                double interpolatedValue = calculateIDWValue(lat, lon, samplePoints, DEFAULT_POWER_P);
                boolean isHotspot = interpolatedValue >= hotspotThreshold;

                grid.add(new InterpolatedPointDTO(lat, lon, interpolatedValue, isHotspot));
            }
        }

        return grid;
    }

    /**
     * Aplica a fórmula matemática do IDW para um ponto (lat, lon) específico.
     */
    public double calculateIDWValue(double lat, double lon, List<SamplePointDTO> samplePoints, double p) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (SamplePointDTO sample : samplePoints) {
            double distanceMeters = calculateHaversineDistanceMeters(
                    lat, lon, sample.latitude(), sample.longitude()
            );

            // Caso o ponto da malha coincida exatamente com uma estação/posto conhecido
            if (distanceMeters < 1.0) {
                return sample.value();
            }

            double weight = 1.0 / Math.pow(distanceMeters, p);
            weightedSum += weight * sample.value();
            totalWeight += weight;
        }

        return totalWeight > 0 ? (weightedSum / totalWeight) : 0.0;
    }

    /**
     * Calcula a distância aproximada em metros entre dois pontos geográficos usando a Fórmula de Haversine.
     */
    private double calculateHaversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusMeters = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusMeters * c;
    }
}