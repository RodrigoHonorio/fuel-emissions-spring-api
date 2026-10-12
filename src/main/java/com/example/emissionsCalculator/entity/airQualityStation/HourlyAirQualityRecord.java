package com.example.emissionsCalculator.entity.airQualityStation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_air_quality_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyAirQualityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "air_quality_station_id")
    private AirQualityStation airQualityStation;

    @Column(name = "measurement_timestamp")
    private LocalDateTime measurementTimestamp;

    @Column(name = "no2")
    private Double no2;

    @Column(name = "pm10")
    private Double pm10;

    @Column(name = "pm25")
    private Double pm25;

    @Column(name = "o3")
    private Double o3;
}