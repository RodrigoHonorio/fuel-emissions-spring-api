package uk.org.spire.emissionsCalculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LondonAirQualityResponse(
        String updatePeriod,
        String forecastURL,
        List<CurrentForecast> currentForecast
) {}