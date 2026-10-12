package com.example.emissionsCalculator.enums.spatial;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PollutantThresholds {

    NO2("NO2", 200.0, "WHO", "WHO Air Quality Guidelines", "WARNING"),
    PM10("PM10", 50.0, "UK_DEFRA", "UK DEFRA Air Quality Strategy", "WARNING"),
    PM25("PM25", 15.0, "WHO", "WHO Global Air Quality Guidelines 2021", "WARNING"),
    O3("O3", 100.0, "WHO", "WHO Air Quality Guidelines", "WARNING"),
    TOTAL_VOC("TOTAL_VOC", 2.0, "UK_DEFRA", "UK Environment Agency / DEFRA Guidance", "DANGER"),
    BENZENE("BENZENE", 5.0, "UK_DEFRA", "UK Air Quality Objectives", "DANGER");

    private final String pollutantType;
    private final double thresholdValue;
    private final String sourceInstitution;
    private final String standardReference;
    private final String defaultSeverity;
}



