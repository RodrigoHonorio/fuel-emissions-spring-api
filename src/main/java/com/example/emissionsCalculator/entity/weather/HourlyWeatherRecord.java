package com.example.emissionsCalculator.entity.weather;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "hourly_weather_records",
        indexes = {
                @Index(name = "idx_weather_forecast_timestamp", columnList = "forecast_timestamp"),
                @Index(name = "idx_weather_location", columnList = "location_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyWeatherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "forecast_timestamp")
    private LocalDateTime forecastTimestamp;

    @Column(name = "temperature_celsius")
    private Double temperatureCelsius;

    @Column(name = "apparent_temperature_celsius")
    private Double apparentTemperatureCelsius;

    @Column(name = "relative_humidity_percentage")
    private Double relativeHumidityPercentage;

    @Column(name = "precipitation_mm")
    private Double precipitationMm;

    @Column(name = "wind_speed_kmh")
    private Double windSpeedKmh;

    @Column(name = "wind_direction_10m")
    private Double windDirection10m;

    @Column(name = "wind_gusts_10m")
    private Double windGusts10m;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}