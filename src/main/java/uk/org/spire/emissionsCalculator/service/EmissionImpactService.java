package uk.org.spire.emissionsCalculator.service;

import org.springframework.stereotype.Service;
import uk.org.spire.emissionsCalculator.dto.GasStationDTO;
import uk.org.spire.emissionsCalculator.dto.VocHeatmapDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmissionImpactService {

    private final GasStationService gasStationService;
    private final GaussianPlumeModel gaussianPlumeModel;

    public EmissionImpactService(GasStationService gasStationService, GaussianPlumeModel gaussianPlumeModel) {
        this.gasStationService = gasStationService;
        this.gaussianPlumeModel = gaussianPlumeModel;
    }

    /**
     * Calcula as plumas individuais e o Heatmap somado de COV para toda a cidade.
     *
     * Princípio da Superposição Linear:
     * C_total(x, y) = Sum( C_i(x - x_i, y - y_i) )
     * Link: https://journals.ametsoc.org/view/journals/apme/36/8/1520-0450_1997_036_1088_itteis_2.0.co_2.xml
     */
    public VocHeatmapDTO calculateCityWideVocPlumesAndHeatmap(
            double temp,
            double humidity,
            double pressure,
            double solarRad,
            double windSpeed,
            double windDirectionDeg,
            String fuelType
    ) {
        List<GasStationDTO> stations = gasStationService.getAllLondonGasStations(temp, 0, humidity, pressure, solarRad, windSpeed, fuelType);

        List<VocHeatmapDTO.PlumeVectorDTO> plumeVectors = new ArrayList<>();
        List<VocHeatmapDTO.HeatmapPointDTO> heatmapPoints = new ArrayList<>();

        double downwindAngleDeg = (windDirectionDeg + 180.0) % 360.0;
        double downwindRad = Math.toRadians(90.0 - downwindAngleDeg);

        double plumeReachMeters = Math.min(1500.0, Math.max(300.0, windSpeed * 250.0));
        for (GasStationDTO station : stations) {
            double deltaLat = (plumeReachMeters * Math.sin(Math.toRadians(downwindAngleDeg))) / 111320.0;
            double deltaLng = (plumeReachMeters * Math.cos(Math.toRadians(downwindAngleDeg))) / (111320.0 * Math.cos(Math.toRadians(station.getLatitude())));

            plumeVectors.add(new VocHeatmapDTO.PlumeVectorDTO(
                    station.getName(),
                    station.getLatitude(),
                    station.getLongitude(),
                    station.getLatitude() + deltaLat,
                    station.getLongitude() + deltaLng,
                    station.getVocEmission()
            ));
        }

        int stepMeters = 80;
        int maxGridRangeMeters = 1200;

        for (GasStationDTO centerStation : stations) {
            for (int dx = -maxGridRangeMeters; dx <= maxGridRangeMeters; dx += stepMeters) {
                for (int dy = -maxGridRangeMeters; dy <= maxGridRangeMeters; dy += stepMeters) {

                    double targetLat = centerStation.getLatitude() + (dy / 111320.0);
                    double targetLng = centerStation.getLongitude() + (dx / (111320.0 * Math.cos(Math.toRadians(centerStation.getLatitude()))));

                    double totalAccumulatedVoc = 0.0;

                    for (GasStationDTO sourceStation : stations) {
                        double dNorth = (targetLat - sourceStation.getLatitude()) * 111320.0;
                        double dEast = (targetLng - sourceStation.getLongitude()) * (111320.0 * Math.cos(Math.toRadians(sourceStation.getLatitude())));

                        double xDownwind = dEast * Math.cos(downwindRad) + dNorth * Math.sin(downwindRad);
                        double yCrosswind = -dEast * Math.sin(downwindRad) + dNorth * Math.cos(downwindRad);

                        if (xDownwind > 0) {
                            double conc = gaussianPlumeModel.calculateConcentration(
                                    sourceStation.getVocEmission(),
                                    xDownwind,
                                    yCrosswind,
                                    windSpeed
                            );
                            totalAccumulatedVoc += conc;
                        }
                    }

                    if (totalAccumulatedVoc > 0.5) {
                        heatmapPoints.add(new VocHeatmapDTO.HeatmapPointDTO(targetLat, targetLng, Math.round(totalAccumulatedVoc * 100.0) / 100.0));
                    }
                }
            }
        }

        return new VocHeatmapDTO(windSpeed, windDirectionDeg, heatmapPoints, plumeVectors);
    }
}