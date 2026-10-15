package com.example.emissionsCalculator.dto.prediction;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WhatIfScenarioRequest {
    private String borough;                  // Ex: "Westminster"
    private Double throughputMultiplier;     // Ex: 1.30 (+30% de tráfego)
    private Double temperatureCelsius;       // Ex: 35.0 (Onda de calor)
    private Double windSpeedKmh;             // Ex: 8.0
    private Double relativeHumidity;         // Ex: 40.0
    private LocalDateTime simulationTimestamp; // Momento hipotético
}
