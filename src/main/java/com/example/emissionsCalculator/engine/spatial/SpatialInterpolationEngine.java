package com.example.emissionsCalculator.engine.spatial;

import com.example.emissionsCalculator.dto.spatial.InterpolatedPointDTO;
import com.example.emissionsCalculator.dto.spatial.SamplePointDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpatialInterpolationEngine {

    private static final double DEFAULT_POWER_P = 2.0; // Expoente padrão do IDW

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

    public double calculateIDWValue(double lat, double lon, List<SamplePointDTO> samplePoints, double p) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (SamplePointDTO sample : samplePoints) {
            double distanceMeters = calculateHaversineDistanceMeters(
                    lat, lon, sample.latitude(), sample.longitude()
            );

            if (distanceMeters < 1.0) {
                return sample.value();
            }

            double weight = 1.0 / Math.pow(distanceMeters, p);
            weightedSum += weight * sample.value();
            totalWeight += weight;
        }

        return totalWeight > 0 ? (weightedSum / totalWeight) : 0.0;
    }

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