package com.example.emissionsCalculator.engine.gasEmissions;

import com.example.emissionsCalculator.dto.gasEmissions.GasStationEmissionResponseDTO;
import com.example.emissionsCalculator.engine.scientific.vocGasStation.StationEmissionsEngine;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.gasEmissions.GasStationEmissionRepository;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import com.example.emissionsCalculator.service.gasEmissions.calculator.EvaporativeEmissionsCalculator;
import com.example.emissionsCalculator.enums.gasEmissions.EmissionModelConstants;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class StationEmissionsCalculatorService {

    private final GasStationEmissionRepository emissionRepository;
    private final HourlyWeatherRecordRepository weatherRepository;
    private final CmaGasStationRepository cmaGasStationRepository;
    private final StationEmissionsEngine stationEmissionsEngine;

    @Async
    @Transactional
    public CompletableFuture<Integer> calculateEmissionsForCurrentHour() {
        log.info("⏳ Iniciando orquestração do cálculo de emissões para a base oficial da CMA...");

        try {
            HourlyWeatherRecord latestWeather = weatherRepository.findFirstByOrderByForecastTimestampDesc()
                    .or(() -> weatherRepository.findFirstByOrderByIdDesc())
                    .orElse(null);

            double tempC = (latestWeather != null && latestWeather.getTemperatureCelsius() != null)
                    ? latestWeather.getTemperatureCelsius()
                    : EmissionModelConstants.DEFAULT_TEMPERATURE_CELSIUS.getValue();

            double windKmh = (latestWeather != null && latestWeather.getWindSpeedKmh() != null)
                    ? latestWeather.getWindSpeedKmh()
                    : EmissionModelConstants.DEFAULT_WIND_SPEED_KMH.getValue();

            double trueVaporPressure = EvaporativeEmissionsCalculator.calculateTrueVaporPressure(tempC);
            double emissionFactor = EvaporativeEmissionsCalculator.calculateEmissionFactor(tempC, trueVaporPressure);

            List<CmaGasStation> cmaStations = cmaGasStationRepository.findAll();
            if (cmaStations.isEmpty()) {
                log.warn("⚠️ Nenhum posto encontrado na tabela da CMA. Execute a ingestão antes do cálculo.");
                return CompletableFuture.completedFuture(0);
            }

            List<GasStationEmission> emissionsToSave = new ArrayList<>(cmaStations.size());
            LocalDateTime now = LocalDateTime.now();

            for (CmaGasStation station : cmaStations) {
                double throughputLiters = EmissionModelConstants.DEFAULT_THROUGHPUT_LITERS_PER_DAY.getValue();

                GasStationEmission emission = EvaporativeEmissionsCalculator.calculateForStation(
                        station.getId(), tempC, windKmh, throughputLiters, emissionFactor, now
                );

                emissionsToSave.add(emission);
            }

            List<GasStationEmission> savedEmissions = emissionRepository.saveAll(emissionsToSave);
            log.info("✅ Processamento da base oficial (EPA) concluído com sucesso para {} postos!", savedEmissions.size());

            try {
                Map<Long, GasStationEmission> baselineMap = savedEmissions.stream()
                        .collect(Collectors.toMap(GasStationEmission::getGasStationId, e -> e));

                stationEmissionsEngine.runExperimentalModels(cmaStations, latestWeather, baselineMap);
            } catch (Exception e) {
                log.error("⚠️ Falha ao executar modelos experimentais: {}", e.getMessage(), e);
            }

            return CompletableFuture.completedFuture(savedEmissions.size());

        } catch (Exception e) {
            log.error("❌ Falha durante o processamento assíncrono de emissões: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Preenchimento histórico (Backfill) hora a hora para os últimos 7 dias.
     */
    @Transactional
    public int calculateEmissionsForPastWeek() {
        log.info("⏳ Iniciando o preenchimento histórico de emissões para os últimos 7 dias...");

        List<CmaGasStation> cmaStations = cmaGasStationRepository.findAll();
        if (cmaStations.isEmpty()) {
            log.warn("⚠️ Nenhum posto encontrado na CMA para o cálculo histórico.");
            return 0;
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);
        int totalRecordsSaved = 0;

        LocalDateTime currentHour = startTime;
        while (!currentHour.isAfter(endTime)) {
            final LocalDateTime targetTime = currentHour;
            HourlyWeatherRecord weather = weatherRepository.findFirstByOrderByForecastTimestampDesc()
                    .orElse(null);

            double tempC = (weather != null && weather.getTemperatureCelsius() != null)
                    ? weather.getTemperatureCelsius()
                    : EmissionModelConstants.DEFAULT_TEMPERATURE_CELSIUS.getValue();

            double windKmh = (weather != null && weather.getWindSpeedKmh() != null)
                    ? weather.getWindSpeedKmh()
                    : EmissionModelConstants.DEFAULT_WIND_SPEED_KMH.getValue();

            double trueVaporPressure = EvaporativeEmissionsCalculator.calculateTrueVaporPressure(tempC);
            double emissionFactor = EvaporativeEmissionsCalculator.calculateEmissionFactor(tempC, trueVaporPressure);

            List<GasStationEmission> emissionsToSave = new ArrayList<>(cmaStations.size());

            for (CmaGasStation station : cmaStations) {
                double throughputLiters = EmissionModelConstants.DEFAULT_THROUGHPUT_LITERS_PER_DAY.getValue() / 24.0;

                GasStationEmission emission = EvaporativeEmissionsCalculator.calculateForStation(
                        station.getId(), tempC, windKmh, throughputLiters, emissionFactor, targetTime
                );

                emissionsToSave.add(emission);
            }

            List<GasStationEmission> saved = emissionRepository.saveAll(emissionsToSave);
            totalRecordsSaved += saved.size();

            currentHour = currentHour.plusHours(1);
        }

        log.info("✅ Preenchimento histórico de 7 dias concluído! Total de {} registos gerados.", totalRecordsSaved);
        return totalRecordsSaved;
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

    @Transactional(readOnly = true)
    public List<GasStationEmissionResponseDTO> getEmissionsForWeek(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(7);
        }

        return emissionRepository.findByCalculationTimestampBetween(startDate, endDate).stream()
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