package com.example.emissionsCalculator.engine.scientific.generalPollution;

import com.example.emissionsCalculator.engine.scientific.vocGasStation.EmissionModelStrategy;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.entity.scientific.generalPollution.ScientificRule;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import com.example.emissionsCalculator.repository.scientific.generalPollution.ScientificRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mvel2.MVEL;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicScientificEmissionModelStrategy implements EmissionModelStrategy {

    private final ScientificRuleRepository ruleRepository;

    @Override
    public String getScientificModelName() {
        return "DYNAMIC_MVEL_SCIENTIFIC_ENGINE";
    }

    @Override
    public ScientificModelResult calculate(CmaGasStation station,
                                           HourlyWeatherRecord weather,
                                           GasStationEmission baselineEmission,
                                           double gasolineLiters,
                                           double ethanolLiters,
                                           LocalDateTime timestamp) {

        List<ScientificRule> rules = ruleRepository.findAll();

        double aggregatedCalculatedPollution = 0.0;
        double baselineVoc = (baselineEmission != null && baselineEmission.getTotalVocKg() != null)
                ? baselineEmission.getTotalVocKg() : 0.0;

        double tempC = (weather != null && weather.getTemperatureCelsius() != null)
                ? weather.getTemperatureCelsius() : 20.0;
        double windSpeed = (weather != null && weather.getWindSpeedKmh() != null)
                ? weather.getWindSpeedKmh() : 12.0;
        double humidity = (weather != null && weather.getRelativeHumidityPercentage() != null)
                ? weather.getRelativeHumidityPercentage() : 60.0;

        // Contexto com todas as variáveis que as fórmulas MVEL possam necessitar
        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("temperature_celsius", tempC);
        contextVariables.put("wind_speed_kmh", windSpeed);
        contextVariables.put("relative_humidity", humidity);
        contextVariables.put("gasoline_volume", gasolineLiters);
        contextVariables.put("ethanol_volume", ethanolLiters);
        contextVariables.put("baseline_voc", baselineVoc);
        contextVariables.put("throughput_liters", gasolineLiters + ethanolLiters);
        contextVariables.put("NOX", 0.0);
        contextVariables.put("CO", 0.0);
        contextVariables.put("BENZENE", baselineVoc * 0.01);

        for (ScientificRule rule : rules) {
            try {
                if (rule.getMvelFormula() == null || rule.getMvelFormula().isBlank()) {
                    continue;
                }

                // Limpeza preventiva de sintaxe corrompida comum em imports antigos
                String formula = rule.getMvelFormula().trim();
                if (formula.endsWith(")\"")) {
                    formula = formula.substring(0, formula.length() - 2);
                }

                Object resultObj = MVEL.eval(formula, contextVariables);

                if (resultObj instanceof Number num) {
                    double ruleResult = num.doubleValue();
                    if (rule.getTargetPollutant() != null &&
                            (rule.getTargetPollutant().toUpperCase().contains("VOC") ||
                                    rule.getTargetPollutant().toUpperCase().contains("PM10") ||
                                    rule.getTargetPollutant().toUpperCase().contains("EMISSION") ||
                                    rule.getTargetPollutant().toUpperCase().contains("POLLUTION"))) {
                        aggregatedCalculatedPollution += ruleResult;
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Regra ignorada por erro de sintaxe MVEL [ID: {}]: {}",
                        rule.getRuleId(), e.getMessage());
            }
        }

        double calculatedTotalVocKg = baselineVoc + aggregatedCalculatedPollution;
        double deltaVocKg = aggregatedCalculatedPollution;
        double deltaPercentage = (baselineVoc > 0) ? (deltaVocKg / baselineVoc) * 100.0 : 0.0;

        return ScientificModelResult.builder()
                .gasStationId(station.getId())
                .modelName(getScientificModelName())
                .calculationTimestamp(timestamp)
                .temperatureCelsius(tempC)
                .gasolineThroughputLiters(gasolineLiters)
                .ethanolThroughputLiters(ethanolLiters)
                .refuelingVocKg(aggregatedCalculatedPollution)
                .calculatedTotalVocKg(calculatedTotalVocKg)
                .baselineVocKg(baselineVoc)
                .deltaVocKg(deltaVocKg)
                .deltaPercentage(deltaPercentage)
                .build();
    }
}