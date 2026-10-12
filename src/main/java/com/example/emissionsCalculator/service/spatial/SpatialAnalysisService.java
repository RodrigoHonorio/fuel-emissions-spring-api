package com.example.emissionsCalculator.service.spatial;

import com.example.emissionsCalculator.dto.spatial.*;
import com.example.emissionsCalculator.engine.spatial.GaussianPlumeCalculator;
import com.example.emissionsCalculator.engine.spatial.SpatialInterpolationEngine;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.gasStation.GasStationRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SpatialAnalysisService {

    private final SpatialInterpolationEngine interpolationEngine;
    private final GaussianPlumeCalculator plumeCalculator;
    private final GasStationRepository gasStationRepository;
    private final HourlyWeatherRecordRepository weatherRepository;

    public SpatialAnalysisService(
            SpatialInterpolationEngine interpolationEngine,
            GaussianPlumeCalculator plumeCalculator,
            GasStationRepository gasStationRepository,
            HourlyWeatherRecordRepository weatherRepository
    ) {
        this.interpolationEngine = interpolationEngine;
        this.plumeCalculator = plumeCalculator;
        this.gasStationRepository = gasStationRepository;
        this.weatherRepository = weatherRepository;
    }

    public List<HeatmapFeatureDTO> getHeatmapData() {
        List<HeatmapFeatureDTO> features = new ArrayList<>();

        gasStationRepository.findAll().forEach(station -> {
            if (station.getLatitude() != null && station.getLongitude() != null) {
                features.add(new HeatmapFeatureDTO(
                        station.getLatitude(),
                        station.getLongitude(),
                        1.0,
                        "GAS_STATION",
                        station.getName() != null ? station.getName() : "Posto de Combustível"
                ));
            }
        });

        return features;
    }

    /**
     * Calcula o mapa de plumas de poluição orientadas pelo vetor de vento meteorológico mais recente.
     */
    public List<PlumePointDTO> getPlumeDispersionData() {
        Optional<HourlyWeatherRecord> latestWeather = weatherRepository.findFirstByOrderByForecastTimestampDesc();

        Double windSpeed = latestWeather.map(HourlyWeatherRecord::getWindSpeedKmh).orElse(10.0);
        Double windDirection = latestWeather.map(HourlyWeatherRecord::getWindDirection10m).orElse(0.0);

        List<PlumePointDTO> allPlumePoints = new ArrayList<>();

        gasStationRepository.findAll().forEach(station -> {
            if (station.getLatitude() != null && station.getLongitude() != null) {
                List<PlumePointDTO> stationPlume = plumeCalculator.calculatePlume(
                        station.getLatitude(),
                        station.getLongitude(),
                        50.0, // Taxa de emissão padrão (Q)
                        windSpeed,
                        windDirection
                );
                allPlumePoints.addAll(stationPlume);
            }
        });

        return allPlumePoints;
    }

    public HotspotAnalysisResponseDTO analyzeHotspots(
            double minLat, double maxLat,
            double minLon, double maxLon,
            double gridSize, double threshold
    ) {
        List<SamplePointDTO> samples = new ArrayList<>();

        gasStationRepository.findAll().forEach(station -> {
            if (station.getLatitude() != null && station.getLongitude() != null) {
                samples.add(new SamplePointDTO(
                        station.getLatitude(),
                        station.getLongitude(),
                        10.0
                ));
            }
        });

        List<InterpolatedPointDTO> grid = interpolationEngine.interpolateGrid(
                samples, minLat, maxLat, minLon, maxLon, gridSize, threshold
        );

        List<InterpolatedPointDTO> hotspots = grid.stream()
                .filter(InterpolatedPointDTO::isHotspot)
                .toList();

        double maxValue = grid.stream()
                .mapToDouble(InterpolatedPointDTO::interpolatedValue)
                .max()
                .orElse(0.0);

        return new HotspotAnalysisResponseDTO(
                LocalDateTime.now(),
                grid.size(),
                hotspots.size(),
                maxValue,
                threshold,
                hotspots
        );
    }
}