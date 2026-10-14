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

/**
 * <b>O que é:</b> Motor de Execução Dinâmica baseado em MVEL.
 * <b>Para que serve:</b> Buscar as regras científicas na base de dados, montar o contexto
 * com variáveis meteorológicas/operacionais e avaliar as fórmulas matemáticas em tempo de execução.
 */
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

        // 1. Busca todas as regras científicas dinâmicas ativas na base de dados
        List<ScientificRule> rules = ruleRepository.findAll();

        if (rules.isEmpty()) {
            log.warn("⚠️ Nenhuma regra científica dinâmica encontrada na base de dados para cálculo.");
        }

        double aggregatedCalculatedPollution = 0.0;
        double baselineVoc = (baselineEmission != null && baselineEmission.getTotalVocKg() != null)
                ? baselineEmission.getTotalVocKg() : 0.0;

        double tempC = (weather != null && weather.getTemperatureCelsius() != null)
                ? weather.getTemperatureCelsius() : 20.0;
        double windSpeed = (weather != null && weather.getWindSpeedKmh() != null)
                ? weather.getWindSpeedKmh() : 12.0;
        double humidity = (weather != null && weather.getRelativeHumidityPercentage() != null)
                ? weather.getRelativeHumidityPercentage() : 60.0;

        // 2. Monta o contexto (mapa de variáveis) que as fórmulas MVEL podem ler dinamicamente
        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("temperature_celsius", tempC);
        contextVariables.put("wind_speed_kmh", windSpeed);
        contextVariables.put("relative_humidity", humidity);
        contextVariables.put("gasoline_volume", gasolineLiters);
        contextVariables.put("ethanol_volume", ethanolLiters);
        contextVariables.put("baseline_voc", baselineVoc);
        contextVariables.put("throughput_liters", gasolineLiters + ethanolLiters);

        // 3. Avalia cada regra cadastrada utilizando o motor MVEL em tempo de execução
        for (ScientificRule rule : rules) {
            try {
                // O MVEL interpreta o texto da fórmula (ex: wind_speed_kmh * 2.1) usando o mapa de variáveis
                Object resultObj = MVEL.eval(rule.getMvelFormula(), contextVariables);

                if (resultObj instanceof Number num) {
                    double ruleResult = num.doubleValue();
                    log.info("Regra [ID: {}, Alvo: {}] avaliada com sucesso. Resultado: {}",
                            rule.getRuleId(), rule.getTargetPollutant(), ruleResult);

                    // Acumula os resultados direcionados ao cálculo de emissões/poluição
                    if (rule.getTargetPollutant() != null &&
                            (rule.getTargetPollutant().toUpperCase().contains("VOC") ||
                                    rule.getTargetPollutant().toUpperCase().contains("PM10") ||
                                    rule.getTargetPollutant().toUpperCase().contains("EMISSION") ||
                                    rule.getTargetPollutant().toUpperCase().contains("POLLUTION"))) {
                        aggregatedCalculatedPollution += ruleResult;
                    }
                }
            } catch (Exception e) {
                log.error("❌ Erro ao avaliar a fórmula MVEL para a regra [{}] ('{}'): {}",
                        rule.getRuleId(), rule.getMvelFormula(), e.getMessage());
            }
        }

        double calculatedTotalVocKg = baselineVoc + aggregatedCalculatedPollution;
        double deltaVocKg = aggregatedCalculatedPollution;
        double deltaPercentage = (baselineVoc > 0) ? (deltaVocKg / baselineVoc) * 100.0 : 0.0;

        // 4. Retorna o resultado estruturado pronto para a pipeline de relatórios
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