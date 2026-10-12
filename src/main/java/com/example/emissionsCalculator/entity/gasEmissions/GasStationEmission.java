package com.example.emissionsCalculator.entity.gasEmissions;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * <b>O que é:</b> Entidade JPA para registo dos cálculos de emissões evaporativas de postos.
 * <b>Para que serve:</b> Armazenar os resultados do modelo físico-químico de emissão de COVs e Benzeno.
 */
@Entity
@Table(name = "gas_station_emissions", indexes = {
        @Index(name = "idx_emissions_station_time", columnList = "gas_station_id, calculation_timestamp DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GasStationEmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gas_station_id", nullable = false)
    private Long gasStationId;

    @Column(name = "calculation_timestamp", nullable = false)
    private LocalDateTime calculationTimestamp;

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

    @Column(name = "wind_speed_kmh")
    private Double windSpeedKmh;

    @Column(name = "estimated_daily_throughput_liters", nullable = false)
    private Double estimatedDailyThroughputLiters;

    @Column(name = "evaporative_voc_kg", nullable = false)
    private Double evaporativeVocKg;

    @Column(name = "spillage_voc_kg", nullable = false)
    private Double spillageVocKg;

    @Column(name = "total_voc_kg", nullable = false)
    private Double totalVocKg;

    @Column(name = "benzene_emissions_g", nullable = false)
    private Double benzeneEmissionsG;
}