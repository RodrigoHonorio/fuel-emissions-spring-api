package com.example.emissionsCalculator.service.scientific.simulation;

import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.generalPollution.ScientificRule;
import com.example.emissionsCalculator.entity.scientific.simulation.SimulationBatch;
import com.example.emissionsCalculator.entity.scientific.simulation.SimulationInputSnapshot;
import com.example.emissionsCalculator.entity.scientific.simulation.SimulationPollutantResult;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import com.example.emissionsCalculator.repository.scientific.generalPollution.ScientificRuleRepository;
import com.example.emissionsCalculator.repository.scientific.simulation.SimulationBatchRepository;
import com.example.emissionsCalculator.repository.scientific.simulation.SimulationInputSnapshotRepository;
import com.example.emissionsCalculator.repository.scientific.simulation.SimulationPollutantResultRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import com.example.emissionsCalculator.service.gasStation.StationThroughputEstimator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulationExecutorWorker {

    private final SimulationBatchRepository batchRepository;
    private final SimulationInputSnapshotRepository snapshotRepository;
    private final SimulationPollutantResultRepository resultRepository;
    private final CmaGasStationRepository stationRepository;
    private final HourlyWeatherRecordRepository weatherRepository;
    private final ScientificRuleRepository ruleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void executeSimulation(Long batchId, LocalDateTime startTime, LocalDateTime endTime, Long parentSimulationId) {
        log.info("⚙️ [TRANSACTIONAL WORKER] A iniciar simulação com volumes reais para o lote ID: {}", batchId);

        SimulationBatch batch = batchRepository.findById(batchId).orElse(null);
        if (batch == null) return;

        try {
            List<CmaGasStation> stations = stationRepository.findAll();
            List<ScientificRule> rules = ruleRepository.findAll();

            Map<String, Serializable> compiledRules = new HashMap<>();
            for (ScientificRule rule : rules) {
                try {
                    Serializable compiled = MVEL.compileExpression(rule.getMvelFormula());
                    compiledRules.put(rule.getRuleId(), compiled);
                } catch (Exception e) {
                    log.error("Erro ao pré-compilar regra MVEL [{}]: {}", rule.getRuleId(), e.getMessage());
                }
            }

            List<HourlyWeatherRecord> weatherRecords = weatherRepository.findAll().stream()
                    .filter(w -> w.getForecastTimestamp() != null &&
                            !w.getForecastTimestamp().isBefore(startTime) &&
                            !w.getForecastTimestamp().isAfter(endTime))
                    .toList();

            Map<Long, Double> parentResultsMap = new HashMap<>();
            if (parentSimulationId != null) {
                List<SimulationPollutantResult> parentResults = resultRepository.findBySimulationBatchId(parentSimulationId);
                for (SimulationPollutantResult pr : parentResults) {
                    parentResultsMap.put(pr.getGasStationId(), pr.getCalculatedValue());
                }
            }

            List<SimulationInputSnapshot> snapshotsToSave = new ArrayList<>();
            List<SimulationPollutantResult> resultsToSave = new ArrayList<>();

            int totalSteps = weatherRecords.size();
            int hourCount = 0;

            for (HourlyWeatherRecord weather : weatherRecords) {
                hourCount++;
                LocalDateTime stepTime = weather.getForecastTimestamp();
                double tempC = weather.getTemperatureCelsius() != null ? weather.getTemperatureCelsius() : 15.0;
                double windKmh = weather.getWindSpeedKmh() != null ? weather.getWindSpeedKmh() : 10.0;
                double humidity = weather.getRelativeHumidityPercentage() != null ? weather.getRelativeHumidityPercentage() : 60.0;

                for (CmaGasStation station : stations) {
                    // Cálculo dinâmico e realista por posto
                    var split = StationThroughputEstimator.estimateThroughput(station);
                    double gasolineLiters = split.gasolineLiters();
                    double ethanolLiters = split.ethanolLiters();
                    double throughput = split.totalLiters();

                    double baselineValue = parentResultsMap.getOrDefault(station.getId(), 2.5);

                    SimulationInputSnapshot snapshot = SimulationInputSnapshot.builder()
                            .simulationBatch(batch)
                            .gasStationId(station.getId())
                            .stepTimestamp(stepTime)
                            .temperatureCelsius(tempC)
                            .windSpeedKmh(windKmh)
                            .relativeHumidity(humidity)
                            .throughputLiters(throughput)
                            .baselineReferenceValue(baselineValue)
                            .build();
                    snapshotsToSave.add(snapshot);

                    Map<String, Object> context = new HashMap<>();
                    context.put("temperature_celsius", tempC);
                    context.put("wind_speed_kmh", windKmh);
                    context.put("relative_humidity", humidity);
                    context.put("gasoline_volume", gasolineLiters);
                    context.put("ethanol_volume", ethanolLiters);
                    context.put("baseline_voc", baselineValue);
                    context.put("throughput_liters", throughput);

                    for (ScientificRule rule : rules) {
                        Serializable compiled = compiledRules.get(rule.getRuleId());
                        if (compiled == null) continue;

                        try {
                            Object evalResult = MVEL.executeExpression(compiled, context);
                            if (evalResult instanceof Number num) {
                                double calculatedValue = num.doubleValue();
                                double deltaValue = calculatedValue - baselineValue;
                                double deltaPercentage = baselineValue > 0 ? (deltaValue / baselineValue) * 100.0 : 0.0;

                                SimulationPollutantResult result = SimulationPollutantResult.builder()
                                        .simulationBatch(batch)
                                        .gasStationId(station.getId())
                                        .stepTimestamp(stepTime)
                                        .ruleId(rule.getRuleId())
                                        .targetPollutant(rule.getTargetPollutant())
                                        .calculatedValue(calculatedValue)
                                        .baselineValue(baselineValue)
                                        .deltaValue(deltaValue)
                                        .deltaPercentage(deltaPercentage)
                                        .unitOfMeasure(rule.getUnitOfMeasure() != null ? rule.getUnitOfMeasure() : "kg")
                                        .build();

                                resultsToSave.add(result);
                            }
                        } catch (Exception e) {
                            log.error("Erro MVEL para regra [{}]: {}", rule.getRuleId(), e.getMessage());
                        }
                    }
                }

                if (snapshotsToSave.size() >= 1000) {
                    snapshotRepository.saveAll(snapshotsToSave);
                    snapshotsToSave.clear();
                }
                if (resultsToSave.size() >= 1000) {
                    resultRepository.saveAll(resultsToSave);
                    resultsToSave.clear();
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            if (!snapshotsToSave.isEmpty()) snapshotRepository.saveAll(snapshotsToSave);
            if (!resultsToSave.isEmpty()) resultRepository.saveAll(resultsToSave);
            entityManager.flush();
            entityManager.clear();

            batch.setStatus("SUCCESS");
            batchRepository.save(batch);
            log.info("✅ [BACKGROUND SUCCESS] Lote ID {} concluído com sucesso!", batchId);

        } catch (Exception e) {
            log.error("❌ [BACKGROUND ERROR] Falha no lote ID {}: {}", batchId, e.getMessage(), e);
            batch.setStatus("FAILED");
            batchRepository.save(batch);
        }
    }
}