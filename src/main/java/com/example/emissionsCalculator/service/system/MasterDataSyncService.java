package com.example.emissionsCalculator.service.system;

import com.example.emissionsCalculator.service.airQualityStation.AirQualityStationIngestionService;
import com.example.emissionsCalculator.service.boundary.LondonBoundaryIngestionService;
import com.example.emissionsCalculator.service.gasEmissions.StationEmissionsCalculatorService;
import com.example.emissionsCalculator.service.gasStation.CmaGasStationIngestionService;
import com.example.emissionsCalculator.service.gasStation.OsmGasStationIngestionService;
import com.example.emissionsCalculator.service.weather.WeatherIngestionService;
import com.example.emissionsCalculator.engine.scientific.vocGasStation.StationEmissionsEngine; // Novo módulo injetado
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    // Injeção do novo motor do Framework S.P.I.R.E.
    private final StationEmissionsEngine scientificEmissionsEngine;

    public String executeFullDataPipeline() {
        log.info("🚀 A iniciar a pipeline completa de sincronização do sistema...");
        StringBuilder summary = new StringBuilder("=== RELATÓRIO DE EXECUÇÃO ===\n");

        try {
            // 1. Limites dos Distritos de Londres
            log.info("1/7. A sincronizar limites territoriais de Londres...");
            int boundaries = boundaryService.syncLondonBoundaries();
            summary.append("1. Distritos de Londres: ").append(boundaries).append(" processados.\n");

            // 2. Postos Oficiais da CMA
            log.info("2/7. A sincronizar postos de combustível CMA...");
            int cmaCount = cmaService.syncCmaGasStations();
            summary.append("2. Postos CMA: ").append(cmaCount).append(" guardados.\n");

            // 3. Postos do OpenStreetMap (OSM) - Tolerante a Timeout
            log.info("3/7. A sincronizar postos OSM para Londres...");
            try {
                int osmCount = osmService.syncOsmGasStationsForLondon();
                summary.append("3. Postos OSM: ").append(osmCount).append(" guardados.\n");
            } catch (Exception e) {
                log.warn("⚠️ Aviso: A sincronização do OSM falhou por timeout ou sobrecarga do Overpass, mas a pipeline prosseguirá: {}", e.getMessage());
                summary.append("3. Postos OSM: Falhou temporariamente (Timeout do Overpass).\n");
            }

            // 4. Estações de Qualidade do Ar (LAQN)
            log.info("4/7. A sincronizar estações de qualidade do ar...");
            int airStations = airQualityService.syncStations();
            summary.append("4. Estações Qualidade Ar: ").append(airStations).append(" processadas.\n");

            // 5. Dados Meteorológicos
            log.info("5/7. A sincronizar série temporal do clima...");
            int weatherCount = weatherService.syncWeatherForecast();
            summary.append("5. Dados de Clima: ").append(weatherCount).append(" registos atualizados.\n");

            // 6. Cálculo de Emissões Base (EPA AP-42)
            log.info("6/7. A calcular emissões base dos postos (Padrão Oficial EPA)...");
            emissionsCalculatorService.calculateEmissionsForCurrentHour();
            summary.append("6. Cálculo de Emissões (Base): Concluído com sucesso.\n");

            // 7. NOVO: Cálculo de Emissões Experimentais (S.P.I.R.E. / CETESB)
            log.info("7/7. A executar modelos científicos experimentais de reabastecimento...");
            try {
                // Invoca o motor S.P.I.R.E. para capturar os dados atuais do banco e gerar os deltas
                scientificEmissionsEngine.executeCurrentHourModels();
                summary.append("7. Modelos S.P.I.R.E (CETESB): Concluído com sucesso.\n");
            } catch (Exception e) {
                log.warn("⚠️ Aviso: Falha ao executar modelos científicos experimentais: {}", e.getMessage());
                summary.append("7. Modelos S.P.I.R.E (CETESB): Falhou (").append(e.getMessage()).append(").\n");
            }

            log.info("✅ Pipeline executada com sucesso!");
            return summary.toString();

        } catch (Exception e) {
            log.error("❌ Falha crítica na pipeline de sincronização: ", e);
            throw new RuntimeException("Erro ao executar pipeline master: " + e.getMessage(), e);
        }
    }
}