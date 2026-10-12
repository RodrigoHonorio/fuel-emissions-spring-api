package com.example.emissionsCalculator.service.spatial;

import com.example.emissionsCalculator.dto.spatial.HotspotResponseDTO;
import com.example.emissionsCalculator.dto.spatial.InterpolatedPointDTO;
import com.example.emissionsCalculator.dto.spatial.PlumePointDTO;
import com.example.emissionsCalculator.dto.spatial.SamplePointDTO;
import com.example.emissionsCalculator.engine.spatial.GaussianPlumeCalculator;
import com.example.emissionsCalculator.engine.spatial.SpatialInterpolationEngine;
import com.example.emissionsCalculator.entity.airQualityStation.AirQualityStation;
import com.example.emissionsCalculator.entity.airQualityStation.HourlyAirQualityRecord;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.GasStation;
import com.example.emissionsCalculator.entity.spatial.SpatialHotspot;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.enums.spatial.PollutantThresholds;
import com.example.emissionsCalculator.repository.airQualityStation.AirQualityStationRepository;
import com.example.emissionsCalculator.repository.airQualityStation.HourlyAirQualityRecordRepository;
import com.example.emissionsCalculator.repository.gasEmissions.GasStationEmissionRepository;
import com.example.emissionsCalculator.repository.gasStation.GasStationRepository;
import com.example.emissionsCalculator.repository.spatial.SpatialHotspotRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpatialHotspotService {

    private final SpatialHotspotRepository hotspotRepository;
    private final AirQualityStationRepository airQualityStationRepository;
    private final HourlyAirQualityRecordRepository hourlyAirQualityRecordRepository;
    private final GasStationRepository gasStationRepository;
    private final GasStationEmissionRepository emissionRepository;
    private final HourlyWeatherRecordRepository weatherRepository;

    private final SpatialInterpolationEngine interpolationEngine;
    private final GaussianPlumeCalculator plumeCalculator;

    @Transactional
    public int generateAndSaveHotspots() {
        log.info("🗺️ A calcular hotspots espaciais para TODOS os poluentes...");

        hotspotRepository.deleteAll();

        List<SpatialHotspot> hotspotsToSave = new ArrayList<>();
        WKTReader reader = new WKTReader();
        LocalDateTime now = LocalDateTime.now();

        // 1. Poluentes Regionais via IDW
        List<AirQualityStation> aqStations = airQualityStationRepository.findAll();
        if (!aqStations.isEmpty()) {
            Map<Long, AirQualityStation> aqStationMap = aqStations.stream()
                    .collect(Collectors.toMap(AirQualityStation::getId, s -> s));

            List<HourlyAirQualityRecord> latestAqRecords = hourlyAirQualityRecordRepository.findAll();

            List<SamplePointDTO> no2Samples = new ArrayList<>();
            List<SamplePointDTO> pm10Samples = new ArrayList<>();
            List<SamplePointDTO> pm25Samples = new ArrayList<>();
            List<SamplePointDTO> o3Samples = new ArrayList<>();

            for (HourlyAirQualityRecord record : latestAqRecords) {
                if (record.getAirQualityStation() == null) continue;
                AirQualityStation st = aqStationMap.get(record.getAirQualityStation().getId());
                if (st == null || st.getLocation() == null) continue;

                double lat = st.getLocation().getY();
                double lon = st.getLocation().getX();

                if (record.getNo2() != null) no2Samples.add(new SamplePointDTO(lat, lon, record.getNo2()));
                if (record.getPm10() != null) pm10Samples.add(new SamplePointDTO(lat, lon, record.getPm10()));
                if (record.getPm25() != null) pm25Samples.add(new SamplePointDTO(lat, lon, record.getPm25()));
                if (record.getO3() != null) o3Samples.add(new SamplePointDTO(lat, lon, record.getO3()));
            }

            double minLat = 51.28, maxLat = 51.69;
            double minLon = -0.51, maxLon = 0.33;
            double gridSize = 0.02;

            if (!no2Samples.isEmpty()) processIdwHotspots(no2Samples, PollutantThresholds.NO2, minLat, maxLat, minLon, maxLon, gridSize, hotspotsToSave, reader, now);
            if (!pm10Samples.isEmpty()) processIdwHotspots(pm10Samples, PollutantThresholds.PM10, minLat, maxLat, minLon, maxLon, gridSize, hotspotsToSave, reader, now);
            if (!pm25Samples.isEmpty()) processIdwHotspots(pm25Samples, PollutantThresholds.PM25, minLat, maxLat, minLon, maxLon, gridSize, hotspotsToSave, reader, now);
            if (!o3Samples.isEmpty()) processIdwHotspots(o3Samples, PollutantThresholds.O3, minLat, maxLat, minLon, maxLon, gridSize, hotspotsToSave, reader, now);
        }

        // 2. Poluentes Locais via Plumas Gaussianas
        Optional<HourlyWeatherRecord> latestWeather = weatherRepository.findFirstByOrderByForecastTimestampDesc();
        Double windSpeed = latestWeather.map(HourlyWeatherRecord::getWindSpeedKmh).orElse(10.0);
        Double windDirection = latestWeather.map(HourlyWeatherRecord::getWindDirection10m).orElse(0.0);

        List<GasStationEmission> emissions = emissionRepository.findLatestEmissionsForAllStations();
        Map<Long, GasStation> stationMap = gasStationRepository.findAll().stream().collect(Collectors.toMap(GasStation::getId, s -> s));

        for (GasStationEmission emission : emissions) {
            GasStation station = stationMap.get(emission.getGasStationId());
            if (station == null || station.getLatitude() == null || station.getLongitude() == null) continue;

            if (emission.getTotalVocKg() >= PollutantThresholds.TOTAL_VOC.getThresholdValue()) {
                List<PlumePointDTO> plumePoints = plumeCalculator.calculatePlume(
                        station.getLatitude(), station.getLongitude(),
                        emission.getTotalVocKg(), windSpeed, windDirection
                );
                for (PlumePointDTO plume : plumePoints) {
                    if (plume.concentration() >= 0.5) {
                        double intensity = plume.concentration() / PollutantThresholds.TOTAL_VOC.getThresholdValue();
                        hotspotsToSave.add(buildHotspot(plume.latitude(), plume.longitude(), PollutantThresholds.TOTAL_VOC, intensity, now, reader));
                    }
                }
            }

            if (emission.getBenzeneEmissionsG() >= PollutantThresholds.BENZENE.getThresholdValue()) {
                List<PlumePointDTO> benzenePlume = plumeCalculator.calculatePlume(
                        station.getLatitude(), station.getLongitude(),
                        emission.getBenzeneEmissionsG() / 1000.0, windSpeed, windDirection
                );
                for (PlumePointDTO plume : benzenePlume) {
                    if (plume.concentration() >= 0.2) {
                        double intensity = plume.concentration() / (PollutantThresholds.BENZENE.getThresholdValue() / 1000.0);
                        hotspotsToSave.add(buildHotspot(plume.latitude(), plume.longitude(), PollutantThresholds.BENZENE, intensity, now, reader));
                    }
                }
            }
        }

        if (!hotspotsToSave.isEmpty()) {
            hotspotRepository.saveAll(hotspotsToSave);
            log.info("✅ Hotspots espaciais salvos com sucesso! Total: {}", hotspotsToSave.size());
        } else {
            log.info("ℹ️ Nenhum ponto atingiu os limiares críticos nesta execução.");
        }

        return hotspotsToSave.size();
    }

    @Transactional(readOnly = true)
    public List<HotspotResponseDTO> getAllHotspotsFromDb() {
        return hotspotRepository.findAll().stream()
                .map(h -> new HotspotResponseDTO(
                        h.getId(),
                        h.getPollutantType(),
                        h.getCalculationTimestamp(),
                        h.getIntensityScore(),
                        h.getSeverityLevel(),
                        h.getSourceInstitution(),
                        h.getStandardReference(),
                        h.getGeometry() != null ? h.getGeometry().getY() : null,
                        h.getGeometry() != null ? h.getGeometry().getX() : null,
                        h.getBoroughCode()
                ))
                .toList();
    }

    private void processIdwHotspots(List<SamplePointDTO> samples, PollutantThresholds threshold,
                                    double minLat, double maxLat, double minLon, double maxLon, double gridSize,
                                    List<SpatialHotspot> targetList, WKTReader reader, LocalDateTime timestamp) {
        List<InterpolatedPointDTO> grid = interpolationEngine.interpolateGrid(
                samples, minLat, maxLat, minLon, maxLon, gridSize, threshold.getThresholdValue()
        );

        for (InterpolatedPointDTO pt : grid) {
            if (pt.isHotspot()) {
                double intensity = pt.interpolatedValue() / threshold.getThresholdValue();
                targetList.add(buildHotspot(pt.latitude(), pt.longitude(), threshold, intensity, timestamp, reader));
            }
        }
    }

    private SpatialHotspot buildHotspot(double lat, double lon, PollutantThresholds threshold, double intensityScore, LocalDateTime timestamp, WKTReader reader) {
        Point point = null;
        try {
            point = (Point) reader.read(String.format("POINT(%s %s)", lon, lat));
        } catch (Exception e) {
            log.error("❌ Erro ao converter geometria para SpatialHotspot: {}", e.getMessage());
        }

        String severity = intensityScore >= 2.0 ? "DANGER" : threshold.getDefaultSeverity();

        return SpatialHotspot.builder()
                .pollutantType(threshold.getPollutantType())
                .calculationTimestamp(timestamp)
                .intensityScore(intensityScore)
                .severityLevel(severity)
                .sourceInstitution(threshold.getSourceInstitution())
                .standardReference(threshold.getStandardReference())
                .geometry(point)
                .boroughCode("LON_ZONE")
                .build();
    }
}