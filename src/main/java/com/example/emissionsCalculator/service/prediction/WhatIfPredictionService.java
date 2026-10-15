package com.example.emissionsCalculator.service.prediction;

import com.example.emissionsCalculator.dto.prediction.WhatIfScenarioRequest;
import com.example.emissionsCalculator.dto.prediction.WhatIfScenarioResponse;
import com.example.emissionsCalculator.engine.scientific.generalPollution.DynamicScientificEmissionModelStrategy;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.gasStation.CmaGasStationRepository;
import com.example.emissionsCalculator.service.gasStation.StationThroughputEstimator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatIfPredictionService {

    private final CmaGasStationRepository stationRepository;
    private final DynamicScientificEmissionModelStrategy mvelEngine;

    public WhatIfScenarioResponse simulateScenario(WhatIfScenarioRequest request) {
        log.info("🔮 A simular cenário hipotético para o borough: [{}] com multiplicador de tráfego: [{}]",
                request.getBorough(), request.getThroughputMultiplier());

        List<CmaGasStation> stations = stationRepository.findAll();

        // Filtragem adaptada aos campos reais de localização da CMA (postcode, city, addressLine1)
        if (request.getBorough() != null && !request.getBorough().isBlank()) {
            String targetBorough = request.getBorough().trim().toLowerCase();
            stations = stations.stream()
                    .filter(s -> matchesBorough(s, targetBorough))
                    .toList();
        }

        double baselineTotal = 0.0;
        double simulatedTotal = 0.0;

        HourlyWeatherRecord customWeather = new HourlyWeatherRecord();
        customWeather.setTemperatureCelsius(request.getTemperatureCelsius() != null ? request.getTemperatureCelsius() : 25.0);
        customWeather.setWindSpeedKmh(request.getWindSpeedKmh() != null ? request.getWindSpeedKmh() : 10.0);
        customWeather.setRelativeHumidityPercentage(request.getRelativeHumidity() != null ? request.getRelativeHumidity() : 50.0);

        LocalDateTime timestamp = request.getSimulationTimestamp() != null ? request.getSimulationTimestamp() : LocalDateTime.now();
        double multiplier = request.getThroughputMultiplier() != null ? request.getThroughputMultiplier() : 1.0;

        for (CmaGasStation station : stations) {
            var standardSplit = StationThroughputEstimator.estimateThroughput(station);

            double hypotheticalGasoline = standardSplit.gasolineLiters() * multiplier;
            double hypotheticalEthanol = standardSplit.ethanolLiters() * multiplier;

            ScientificModelResult result = mvelEngine.calculate(
                    station,
                    customWeather,
                    null,
                    hypotheticalGasoline,
                    hypotheticalEthanol,
                    timestamp
            );

            baselineTotal += standardSplit.totalLiters() * 0.001;
            simulatedTotal += result.getRefuelingVocKg();
        }

        double absoluteDelta = simulatedTotal - baselineTotal;
        double percentageInc = (baselineTotal > 0) ? (absoluteDelta / baselineTotal) * 100.0 : 0.0;

        String description = String.format("Cenário simulado para %d postos em %s: Temperatura %.1f°C, Multiplicador de tráfego x%.2f",
                stations.size(),
                request.getBorough() != null ? request.getBorough() : "Greater London",
                customWeather.getTemperatureCelsius(),
                multiplier);

        return WhatIfScenarioResponse.builder()
                .borough(request.getBorough() != null ? request.getBorough() : "Greater London")
                .affectedStationsCount(stations.size())
                .baselineTotalVocKg(baselineTotal)
                .simulatedTotalVocKg(simulatedTotal)
                .absoluteDeltaVocKg(absoluteDelta)
                .percentageIncrease(percentageInc)
                .scenarioDescription(description)
                .build();
    }

    private boolean matchesBorough(CmaGasStation station, String targetBorough) {
        if (station == null) return false;

        boolean inCity = station.getCity() != null && station.getCity().toLowerCase().contains(targetBorough);
        boolean inCounty = station.getCounty() != null && station.getCounty().toLowerCase().contains(targetBorough);
        boolean inAddress = station.getAddressLine1() != null && station.getAddressLine1().toLowerCase().contains(targetBorough);

        // Mapeamento especial para códigos postais centrais caso pesquise por Westminster / Central London
        boolean isCentralPostcode = false;
        if (station.getPostcode() != null) {
            String pc = station.getPostcode().trim().toUpperCase();
            if (targetBorough.contains("westminster") || targetBorough.contains("central")) {
                isCentralPostcode = pc.startsWith("SW1") || pc.startsWith("W1") || pc.startsWith("WC") || pc.startsWith("EC");
            }
        }

        return inCity || inCounty || inAddress || isCentralPostcode;
    }
}