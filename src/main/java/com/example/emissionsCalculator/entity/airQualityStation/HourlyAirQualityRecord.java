package com.example.emissionsCalculator.entity.airQualityStation;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Entidade JPA para registro de medições horárias de poluentes.
 * <b>Para que serve:</b> Armazenar a série temporal de NO2, PM10, PM2.5 e O3 associada a uma estação.
 * <b>De onde veio:</b> Tabela 'hourly_air_quality_records'.
 */
@Entity
@Table(name = "hourly_air_quality_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_station_measurement_time", columnNames = {"station_id", "measurementTimestamp"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyAirQualityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private AirQualityStation station;

    @Column(nullable = false)
    private LocalDateTime measurementTimestamp;

    private Double no2Ugm3;

    private Double pm10Ugm3;

    private Double pm25Ugm3;

    private Double o3Ugm3;

    private Integer airQualityIndex;

    private LocalDateTime updatedAt;
}
