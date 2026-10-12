package com.example.emissionsCalculator.engine.scientific.vocGasStation;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;

import com.example.emissionsCalculator.repository.gasEmissions.GasStationEmissionRepository;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import com.example.emissionsCalculator.repository.scientific.vocGasStation.ScientificModelResultRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <b>O que é:</b> Motor de orquestração para os modelos contidos em scientific.vocGasStation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationEmissionsEngine {

    private final List<EmissionModelStrategy> activeScientificModels;
    private final ScientificModelResultRepository resultRepository;

    private final CmaGasStationRepository stationRepository;
    private final HourlyWeatherRecordRepository weatherRepository;
    private final GasStationEmissionRepository emissionRepository;

    @Transactional
    public void executeCurrentHourModels() {
        log.info("🔍 A recolher dados mais recentes da base para o Framework S.P.I.R.E...");

        List<CmaGasStation> stations = stationRepository.findAll();

        // Utilizando o método correto do repositório alinhado à entidade
        HourlyWeatherRecord weather = weatherRepository.findFirstByOrderByForecastTimestampDesc().orElse(null);

        List<GasStationEmission> recentEmissions = emissionRepository.findAll();
        Map<Long, GasStationEmission> baselineMap = recentEmissions.stream()
                .collect(Collectors.toMap(
                        GasStationEmission::getGasStationId,
                        e -> e,
                        (existente, substituto) -> existente
                ));

        runExperimentalModels(stations, weather, baselineMap);
    }

    public void runExperimentalModels(List<CmaGasStation> stations,
                                      HourlyWeatherRecord weather,
                                      Map<Long, GasStationEmission> baselineMap) {

        if (activeScientificModels.isEmpty()) {
            return;
        }

        log.info("🧪 A executar {} modelo(s) no pacote scientific.vocGasStation...", activeScientificModels.size());

        List<ScientificModelResult> resultsToSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (CmaGasStation station : stations) {
            GasStationEmission baseline = baselineMap.get(station.getId());

            double gasolineLiters = 4500.0;
            double ethanolLiters = 500.0;

            for (EmissionModelStrategy model : activeScientificModels) {
                ScientificModelResult result = model.calculate(station, weather, baseline, gasolineLiters, ethanolLiters, now);
                resultsToSave.add(result);
            }
        }

        resultRepository.saveAll(resultsToSave);
        log.info("✅ {} resultados salvos na tabela 'scientific_model_results'!", resultsToSave.size());
    }
}