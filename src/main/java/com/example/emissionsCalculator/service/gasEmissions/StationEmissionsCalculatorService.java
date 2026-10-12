package com.example.emissionsCalculator.service.gasEmissions;

import com.example.emissionsCalculator.dto.gasEmissions.GasStationEmissionResponseDTO;
import com.example.emissionsCalculator.engine.scientific.vocGasStation.StationEmissionsEngine;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.gasEmissions.GasStationEmissionRepository;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import com.example.emissionsCalculator.service.gasEmissions.calculator.EvaporativeEmissionsCalculator;
import com.example.emissionsCalculator.service.gasEmissions.constants.EmissionModelConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <b>O que é:</b> Serviço responsável pela orquestração do cálculo de emissões.
 * <b>Para que serve:</b> Buscar variáveis climáticas recentes, iterar sobre os postos oficiais da CMA,
 * persistir a base oficial e acionar o motor científico experimental de forma isolada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationEmissionsCalculatorService {

    private final GasStationEmissionRepository emissionRepository;
    private final HourlyWeatherRecordRepository weatherRepository;
    private final CmaGasStationRepository cmaGasStationRepository;
    private final StationEmissionsEngine stationEmissionsEngine; // <-- INJEÇÃO DO MOTOR CIENTÍFICO

    /**
     * Executa o cálculo de emissões em segundo plano para todos os postos oficiais da CMA.
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> calculateEmissionsForCurrentHour() {
        log.info("⏳ Iniciando orquestração do cálculo de emissões para a base oficial da CMA...");

        try {
            // 1. Obtenção otimizada da medição meteorológica mais recente
            HourlyWeatherRecord latestWeather = weatherRepository.findFirstByOrderByForecastTimestampDesc()
                    .or(() -> weatherRepository.findFirstByOrderByIdDesc())
                    .orElse(null);

            double tempC = (latestWeather != null && latestWeather.getTemperatureCelsius() != null)
                    ? latestWeather.getTemperatureCelsius()
                    : EmissionModelConstants.DEFAULT_TEMPERATURE_CELSIUS.getValue();

            double windKmh = (latestWeather != null && latestWeather.getWindSpeedKmh() != null)
                    ? latestWeather.getWindSpeedKmh()
                    : EmissionModelConstants.DEFAULT_WIND_SPEED_KMH.getValue();

            // 2. Execução dos cálculos científicos universais (Antoine / EPA AP-42)
            double trueVaporPressure = EvaporativeEmissionsCalculator.calculateTrueVaporPressure(tempC);
            double emissionFactor = EvaporativeEmissionsCalculator.calculateEmissionFactor(tempC, trueVaporPressure);

            // 3. Consulta de todos os postos oficiais cadastrados
            List<CmaGasStation> cmaStations = cmaGasStationRepository.findAll();
            if (cmaStations.isEmpty()) {
                log.warn("⚠️ Nenhum posto encontrado na tabela da CMA. Execute a ingestão antes do cálculo.");
                return CompletableFuture.completedFuture(0);
            }

            log.info("🔍 Processando emissões para {} postos oficiais...", cmaStations.size());

            List<GasStationEmission> emissionsToSave = new ArrayList<>(cmaStations.size());
            LocalDateTime now = LocalDateTime.now();

            // 4. Processamento iterativo para cada posto da CMA
            for (CmaGasStation station : cmaStations) {
                double throughputLiters = EmissionModelConstants.DEFAULT_THROUGHPUT_LITERS_PER_DAY.getValue();

                GasStationEmission emission = EvaporativeEmissionsCalculator.calculateForStation(
                        station.getId(), tempC, windKmh, throughputLiters, emissionFactor, now
                );

                emissionsToSave.add(emission);
            }

            // 5. Persistência em lote de todas as emissões da base oficial (EPA)
            List<GasStationEmission> savedEmissions = emissionRepository.saveAll(emissionsToSave);
            log.info("✅ Processamento da base oficial (EPA) concluído com sucesso para {} postos!", savedEmissions.size());

            // 6. EXECUÇÃO DOS MODELOS CIENTÍFICOS EXPERIMENTAIS (CETESB, ETC)
            try {
                Map<Long, GasStationEmission> baselineMap = savedEmissions.stream()
                        .collect(Collectors.toMap(GasStationEmission::getGasStationId, e -> e));

                stationEmissionsEngine.runExperimentalModels(cmaStations, latestWeather, baselineMap);
            } catch (Exception e) {
                log.error("⚠️ Falha ao executar modelos experimentais (o cálculo base foi mantido): {}", e.getMessage(), e);
            }

            return CompletableFuture.completedFuture(savedEmissions.size());

        } catch (Exception e) {
            log.error("❌ Falha durante o processamento assíncrono de emissões: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Transactional(readOnly = true)
    public Page<GasStationEmissionResponseDTO> getEmissions(Pageable pageable) {
        return emissionRepository.findAll(pageable)
                .map(e -> new GasStationEmissionResponseDTO(
                        e.getId(),
                        e.getGasStationId(),
                        e.getCalculationTimestamp(),
                        e.getTemperatureCelsius(),
                        e.getWindSpeedKmh(),
                        e.getEstimatedDailyThroughputLiters(),
                        e.getEvaporativeVocKg(),
                        e.getSpillageVocKg(),
                        e.getTotalVocKg(),
                        e.getBenzeneEmissionsG()
                ));
    }

    @Transactional(readOnly = true)
    public List<GasStationEmissionResponseDTO> getAllEmissions() {
        return emissionRepository.findAll().stream()
                .map(e -> new GasStationEmissionResponseDTO(
                        e.getId(),
                        e.getGasStationId(),
                        e.getCalculationTimestamp(),
                        e.getTemperatureCelsius(),
                        e.getWindSpeedKmh(),
                        e.getEstimatedDailyThroughputLiters(),
                        e.getEvaporativeVocKg(),
                        e.getSpillageVocKg(),
                        e.getTotalVocKg(),
                        e.getBenzeneEmissionsG()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GasStationEmissionResponseDTO> getLatestEmissionsForMap() {
        return emissionRepository.findLatestEmissionsForAllStations().stream()
                .map(e -> new GasStationEmissionResponseDTO(
                        e.getId(),
                        e.getGasStationId(),
                        e.getCalculationTimestamp(),
                        e.getTemperatureCelsius(),
                        e.getWindSpeedKmh(),
                        e.getEstimatedDailyThroughputLiters(),
                        e.getEvaporativeVocKg(),
                        e.getSpillageVocKg(),
                        e.getTotalVocKg(),
                        e.getBenzeneEmissionsG()
                ))
                .toList();
    }
}