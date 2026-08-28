package uk.org.spire.emissionsCalculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrentForecast(
        String forecastType,
        String forecastBand,
        String forecastSummary,
        String nO2Band,
        String pM10Band,
        String pM25Band
) {}