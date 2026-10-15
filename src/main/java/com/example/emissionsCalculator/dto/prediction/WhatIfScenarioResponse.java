package com.example.emissionsCalculator.dto.prediction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WhatIfScenarioResponse {
    private String borough;
    private int affectedStationsCount;
    private double baselineTotalVocKg;
    private double simulatedTotalVocKg;
    private double absoluteDeltaVocKg;
    private double percentageIncrease;
    private String scenarioDescription;
}