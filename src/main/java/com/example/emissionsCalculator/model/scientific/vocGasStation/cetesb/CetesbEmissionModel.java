package com.example.emissionsCalculator.model.scientific.vocGasStation.cetesb;

import com.example.emissionsCalculator.engine.scientific.vocGasStation.EmissionModelStrategy;
import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult;
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Modelo de cálculo de emissões no abastecimento baseado no artigo CETESB 2015.
 * <b>Para que serve:</b> Executar a fórmula Tg = C * Ce e guardar na tabela 'scientific_model_results'.
 */
@Service
@ConditionalOnProperty(name = "scientific.models.cetesb.enabled", havingValue = "true")
public class CetesbEmissionModel implements EmissionModelStrategy {

    private static final double CE_GASOLINE = 1.14; // g/L (Taxa de evaporação)
    private static final double CE_ETHANOL = 0.37;  // g/L (Taxa de evaporação)

    @Override
    public String getScientificModelName() {
        return "CETESB_2015_REFUELING";
    }

    @Override
    public ScientificModelResult calculate(CmaGasStation station,
                                           HourlyWeatherRecord weather,
                                           GasStationEmission baselineEmission,
                                           double gasolineLiters,
                                           double ethanolLiters,
                                           LocalDateTime timestamp) {

        double tempC = (weather != null && weather.getTemperatureCelsius() != null)
                ? weather.getTemperatureCelsius() : 15.0;

        double baselineVoc = (baselineEmission != null && baselineEmission.getTotalVocKg() != null)
                ? baselineEmission.getTotalVocKg() : 0.0;

        // Fórumula CETESB 2015: Tg = C * Ce
        double refuelingGasolineKg = (gasolineLiters * CE_GASOLINE) / 1000.0;
        double refuelingEthanolKg = (ethanolLiters * CE_ETHANOL) / 1000.0;
        double refuelingVocKg = refuelingGasolineKg + refuelingEthanolKg;

        double calculatedTotalVocKg = baselineVoc + refuelingVocKg;
        double deltaVocKg = refuelingVocKg;
        double deltaPercentage = (baselineVoc > 0) ? (deltaVocKg / baselineVoc) * 100.0 : 0.0;

        return ScientificModelResult.builder()
                .gasStationId(station.getId())
                .modelName(getScientificModelName())
                .calculationTimestamp(timestamp)
                .temperatureCelsius(tempC)
                .gasolineThroughputLiters(gasolineLiters)
                .ethanolThroughputLiters(ethanolLiters)
                .refuelingVocKg(refuelingVocKg)
                .calculatedTotalVocKg(calculatedTotalVocKg)
                .baselineVocKg(baselineVoc)
                .deltaVocKg(deltaVocKg)
                .deltaPercentage(deltaPercentage)
                .build();
    }
}
