package com.example.emissionsCalculator.dto.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenMeteoResponseDTO(
        @JsonProperty("latitude") Double latitude,
        @JsonProperty("longitude") Double longitude,
        @JsonProperty("generationtime_ms") Double generationtimeMs,
        @JsonProperty("utc_offset_seconds") Integer utcOffsetSeconds,
        @JsonProperty("timezone") String timezone,
        @JsonProperty("timezone_abbreviation") String timezoneAbbreviation,
        @JsonProperty("elevation") Double elevation,
        @JsonProperty("hourly_units") HourlyUnits hourlyUnits,
        @JsonProperty("hourly") HourlyData hourly
) {
    public record HourlyUnits(
            @JsonProperty("time") String time,
            @JsonProperty("temperature_2m") String temperature2m,
            @JsonProperty("relative_humidity_2m") String relativeHumidity2m,
            @JsonProperty("apparent_temperature") String apparentTemperature,
            @JsonProperty("precipitation") String precipitation,
            @JsonProperty("wind_speed_10m") String windSpeed10m,
            @JsonProperty("wind_direction_10m") String windDirection10m,
            @JsonProperty("wind_gusts_10m") String windGusts10m
    ) {}

    public record HourlyData(
            @JsonProperty("time") List<String> time,
            @JsonProperty("temperature_2m") List<Double> temperature2m,
            @JsonProperty("relative_humidity_2m") List<Double> relativeHumidity2m,
            @JsonProperty("apparent_temperature") List<Double> apparentTemperature,
            @JsonProperty("precipitation") List<Double> precipitation,
            @JsonProperty("wind_speed_10m") List<Double> windSpeed10m,
            @JsonProperty("wind_direction_10m") List<Double> windDirection10m,
            @JsonProperty("wind_gusts_10m") List<Double> windGusts10m
    ) {}
}