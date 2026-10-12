package com.example.emissionsCalculator.engine.scientific.vocGasStation;

import com.example.emissionsCalculator.entity.gasEmissions.GasStationEmission;
import com.example.emissionsCalculator.entity.gasStation.CmaGasStation;
import com.example.emissionsCalculator.entity.scientific.vocGasStation.ScientificModelResult; // <-- Import correto aqui!
import com.example.emissionsCalculator.entity.weather.HourlyWeatherRecord;

import java.time.LocalDateTime;

public interface EmissionModelStrategy {

    String getScientificModelName();

    ScientificModelResult calculate(CmaGasStation station,
                                    HourlyWeatherRecord weather,
                                    GasStationEmission baselineEmission,
                                    double gasolineLiters,
                                    double ethanolLiters,
                                    LocalDateTime timestamp);
}