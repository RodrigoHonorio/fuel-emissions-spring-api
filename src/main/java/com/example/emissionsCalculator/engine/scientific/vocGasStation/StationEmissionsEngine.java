package com.example.emissionsCalculator.engine.scientific.vocGasStation;

import com.example.emissionsCalculator.engine.scientific.vocGasStation.EmissionModelStrategy;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.gasEmissions.GasStationEmissionRepository;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import com.example.emissionsCalculator.repository.weather.HourlyWeatherRecordRepository;
import com.example.emissionsCalculator.repository.scientific.vocGasStation.ScientificModelResultRepository;
import com.example.emissionsCalculator.service.gasStation.StationThroughputEstimator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        log.info("🧪 A recolher dados para o Framework S.P.I.R.E com volumes dinâmicos...");

        List<CmaGasStation> stations = stationRepository.findAll();
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

        if (activeScientificModels.isEmpty()) return;

        log.info("🧪 A executar {} modelo(s) científicos com volumes reais...", activeScientificModels.size());

        List<ScientificModelResult> resultsToSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (CmaGasStation station : stations) {
            GasStationEmission baseline = baselineMap.get(station.getId());

            // Uso do estimador dinâmico real
            var split = StationThroughputEstimator.estimateThroughput(station);
            double gasolineLiters = split.gasolineLiters();
            double ethanolLiters = split.ethanolLiters();

            for (EmissionModelStrategy model : activeScientificModels) {
                ScientificModelResult result = model.calculate(station, weather, baseline, gasolineLiters, ethanolLiters, now);
                resultsToSave.add(result);
            }
        }

        resultRepository.saveAll(resultsToSave);
        log.info("✅ {} resultados salvos com volumes dinâmicos!", resultsToSave.size());
    }
}