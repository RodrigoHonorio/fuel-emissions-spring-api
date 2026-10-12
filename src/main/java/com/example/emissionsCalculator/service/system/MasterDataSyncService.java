package com.example.emissionsCalculator.service.system;

import com.example.emissionsCalculator.service.airQualityStation.AirQualityStationIngestionService;
import com.example.emissionsCalculator.service.boundary.LondonBoundaryIngestionService;
import com.example.emissionsCalculator.engine.gasEmissions.StationEmissionsCalculatorService;
import com.example.emissionsCalculator.service.gasStation.CmaGasStationIngestionService;
import com.example.emissionsCalculator.service.gasStation.OsmGasStationIngestionService;
import com.example.emissionsCalculator.service.weather.WeatherIngestionService;
import com.example.emissionsCalculator.service.spatial.SpatialHotspotService;
import com.example.emissionsCalculator.engine.scientific.vocGasStation.StationEmissionsEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterDataSyncService {

    private final LondonBoundaryIngestionService boundaryService;
    private final CmaGasStationIngestionService cmaService;
    private final OsmGasStationIngestionService osmService;
    private final AirQualityStationIngestionService airQualityService;
    private final WeatherIngestionService weatherService;
    private final StationEmissionsCalculatorService emissionsCalculatorService;
    private final StationEmissionsEngine scientificEmissionsEngine;
    private final SpatialHotspotService spatialHotspotService;

    public String executeFullDataPipeline() {
        log.info("🚀 A iniciar a pipeline completa de sincronização do sistema...");
        StringBuilder summary = new StringBuilder("=== RELATÓRIO DE EXECUÇÃO ===\n");

        try {
            int boundaries = boundaryService.syncLondonBoundaries();
            summary.append("1. Distritos de Londres: ").append(boundaries).append(" processados.\n");

            int cmaCount = cmaService.syncCmaGasStations();
            summary.append("2. Postos CMA: ").append(cmaCount).append(" guardados.\n");

            int osmCount = osmService.syncOsmGasStationsForLondon();
            summary.append("3. Postos OSM: ").append(osmCount).append(" guardados.\n");

            int airStations = airQualityService.syncStations();
            int hourlyAirQualityCount = airQualityService.syncHourlyAirQualityPastWeek();
            summary.append("4. Estações Qualidade Ar: ").append(airStations)
                    .append(" (Registos horários 7 dias: ").append(hourlyAirQualityCount).append(").\n");

            int weatherCount = weatherService.syncWeatherForecast();
            summary.append("5. Dados de Clima: ").append(weatherCount).append(" registos atualizados.\n");

            emissionsCalculatorService.calculateEmissionsForPastWeek();
            summary.append("6. Cálculo de Emissões Históricas (7 dias): Concluído com sucesso.\n");

            scientificEmissionsEngine.executeCurrentHourModels();
            summary.append("7. Modelos S.P.I.R.E (CETESB): Concluído com sucesso.\n");

            int hotspotsCount = spatialHotspotService.generateAndSaveHotspots();
            summary.append("8. Hotspots Espaciais Críticos: ").append(hotspotsCount).append(" detetados e gravados.\n");

            log.info("✅ Pipeline executada com sucesso!");
            return summary.toString();

        } catch (Exception e) {
            log.error("❌ Falha crítica na pipeline de sincronização: ", e);
            throw new RuntimeException("Erro ao executar pipeline master: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> executeFullPipelineMap() {
        log.info("🚀 A iniciar a pipeline completa de sincronização do sistema (Formato JSON)...");
        long startTime = System.currentTimeMillis();
        Map<String, Object> report = new HashMap<>();

        try {
            int boundaries = boundaryService.syncLondonBoundaries();
            int cmaCount = cmaService.syncCmaGasStations();
            int osmCount = osmService.syncOsmGasStationsForLondon();
            int airStations = airQualityService.syncStations();
            int hourlyAirQualityCount = airQualityService.syncHourlyAirQualityPastWeek();
            int weatherCount = weatherService.syncWeatherForecast();

            emissionsCalculatorService.calculateEmissionsForPastWeek();

            int hotspotsCount = spatialHotspotService.generateAndSaveHotspots();

            try {
                scientificEmissionsEngine.executeCurrentHourModels();
                report.put("scientificModelsStatus", "SUCCESS");
            } catch (Exception e) {
                log.warn("⚠️ Aviso: Falha nos modelos científicos: {}", e.getMessage());
                report.put("scientificModelsStatus", "FAILED: " + e.getMessage());
            }

            long executionTime = System.currentTimeMillis() - startTime;

            report.put("status", "SUCCESS");
            report.put("executionTimeMs", executionTime);
            report.put("boundariesProcessed", boundaries);
            report.put("cmaStationsProcessed", cmaCount);
            report.put("osmStationsProcessed", osmCount);
            report.put("airStationsProcessed", airStations);
            report.put("hourlyAirQualityRecordsProcessed", hourlyAirQualityCount);
            report.put("weatherRecordsUpdated", weatherCount);
            report.put("spatialHotspotsDetected", hotspotsCount);

            log.info("✅ Pipeline JSON executada com sucesso em {} ms!", executionTime);
            return report;

        } catch (Exception e) {
            log.error("❌ Falha crítica na pipeline de sincronização JSON: ", e);
            throw new RuntimeException("Erro ao executar pipeline master: " + e.getMessage(), e);
        }
    }
}